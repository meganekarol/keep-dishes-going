// Stands in for the external delivery service (US-28, US-29).
//
// Listens for the orders Keep Dishes Going hands over and answers the way a
// courier company would: picked up a while after the kitchen says ready, a
// stream of positions along the way, then delivered. It speaks the same
// RabbitMQ contract as the real service, so the backend cannot tell the
// difference.

import amqp from "amqplib";
import { randomUUID } from "node:crypto";

const RABBITMQ_URL = process.env.RABBITMQ_URL ?? "amqp://user:password@localhost:5672";
const EXCHANGE = "kdg.events";
const QUEUE = "delivery-simulator.orders";
const COURIER_ID = process.env.COURIER_ID ?? "simulator";

const PICKUP_DELAY_MS = seconds(process.env.PICKUP_DELAY_SECONDS, 10);
const LOCATION_INTERVAL_MS = seconds(process.env.LOCATION_INTERVAL_SECONDS, 3);
const LOCATION_UPDATES = Number(process.env.LOCATION_UPDATES ?? 10);

// What an accepted order told us about its journey, kept until it is ready.
const journeys = new Map();

function seconds(value, fallback) {
  return Number(value ?? fallback) * 1000;
}

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

// The backend reads occurredAt as a LocalDateTime, which has no zone.
const now = () => new Date().toISOString().replace("Z", "");

async function connect() {
  for (let attempt = 1; ; attempt++) {
    try {
      return await amqp.connect(RABBITMQ_URL);
    } catch (error) {
      console.log(`RabbitMQ not reachable yet (attempt ${attempt}): ${error.message}`);
      await sleep(3000);
    }
  }
}

function publish(channel, step, order, extra = {}) {
  const message = {
    eventId: randomUUID(),
    occurredAt: now(),
    restaurantId: order.restaurantId,
    orderId: order.orderId,
    ...extra,
  };
  channel.publish(
    EXCHANGE,
    `delivery.${COURIER_ID}.order.${step}.v1`,
    Buffer.from(JSON.stringify(message)),
    { contentType: "application/json", persistent: true },
  );
  console.log(`-> ${step} for order ${order.orderId}`);
}

function onAccepted(event) {
  journeys.set(event.orderId, {
    from: event.pickupCoordinates ?? null,
    to: event.dropoffCoordinates ?? null,
  });
  console.log(`<- accepted order ${event.orderId}, waiting for the kitchen`);
}

async function onReady(channel, event) {
  console.log(`<- order ${event.orderId} is ready, courier on the way`);
  const journey = journeys.get(event.orderId);
  journeys.delete(event.orderId);

  await sleep(PICKUP_DELAY_MS);
  publish(channel, "pickedup", event);

  // Without both ends of the trip there is no route to report, so the order
  // goes straight from picked up to delivered.
  if (journey?.from && journey?.to) {
    for (let i = 1; i <= LOCATION_UPDATES; i++) {
      await sleep(LOCATION_INTERVAL_MS);
      const t = i / LOCATION_UPDATES;
      publish(channel, "location", event, {
        lat: journey.from.lat + (journey.to.lat - journey.from.lat) * t,
        lng: journey.from.lng + (journey.to.lng - journey.from.lng) * t,
      });
    }
  } else {
    await sleep(LOCATION_INTERVAL_MS);
  }

  publish(channel, "delivered", event);
}

async function main() {
  const connection = await connect();
  connection.on("close", () => {
    console.log("RabbitMQ connection closed, exiting so Docker restarts us");
    process.exit(1);
  });

  const channel = await connection.createChannel();
  await channel.assertExchange(EXCHANGE, "topic", { durable: true });
  await channel.assertQueue(QUEUE, { durable: true });
  await channel.bindQueue(QUEUE, EXCHANGE, "restaurant.*.order.accepted.v1");
  await channel.bindQueue(QUEUE, EXCHANGE, "restaurant.*.order.ready.v1");

  await channel.consume(QUEUE, (message) => {
    if (!message) return;
    channel.ack(message);

    let event;
    try {
      event = JSON.parse(message.content.toString());
    } catch {
      console.log(`Ignoring a message that is not JSON: ${message.fields.routingKey}`);
      return;
    }

    const routingKey = message.fields.routingKey;
    if (routingKey.endsWith(".order.accepted.v1")) {
      onAccepted(event);
    } else if (routingKey.endsWith(".order.ready.v1")) {
      onReady(channel, event).catch((error) =>
        console.log(`Delivery of order ${event.orderId} failed: ${error.message}`));
    }
  });

  console.log(`Delivery simulator listening on ${EXCHANGE} as courier "${COURIER_ID}"`);
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
