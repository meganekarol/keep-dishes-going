package be.kdg.keepdishesgoing.order.port.in;

import be.kdg.keepdishesgoing.order.port.in.commands.RecordCourierLocationCommand;

/**
 * Keeps the last known position of the courier carrying an order (US-29).
 */
public interface RecordCourierLocationUseCase {

    void recordCourierLocation(RecordCourierLocationCommand command);
}
