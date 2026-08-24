import { useContext } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Navigate, useNavigate } from 'react-router-dom';
import { Alert, AlertTitle, Button, Divider, Paper, Stack, Typography } from '@mui/material';

import { FormTextField } from '../components/form/FormTextField';
import BasketContext from '../context/BasketContext';
import { UNAVAILABLE_LABEL, useBasketAvailability } from '../hooks/useBasketAvailability';
import { usePlaceOrder } from '../hooks/useOrderTracking';
import { usePlacedOrders } from '../hooks/usePlacedOrders';
import { formatPrice } from '../utils/format';

const checkoutFormSchema = z.object({
  customerName: z.string().trim().min(1, 'We need a name for the order'),
  customerEmail: z
    .string()
    .trim()
    .regex(/^\S+@\S+\.\S+$/, 'That does not look like an email'),
  deliveryStreet: z.string().trim().min(1, 'Street is required'),
  deliveryNumber: z.string().trim().min(1, 'Number is required'),
  deliveryPostalCode: z.string().trim().min(1, 'Postal code is required'),
  deliveryCity: z.string().trim().min(1, 'City is required'),
  deliveryCountry: z.string().trim().min(1, 'Country is required'),
});

type CheckoutFormValues = z.infer<typeof checkoutFormSchema>;

const EMPTY_CHECKOUT: CheckoutFormValues = {
  customerName: '',
  customerEmail: '',
  deliveryStreet: '',
  deliveryNumber: '',
  deliveryPostalCode: '',
  deliveryCity: '',
  deliveryCountry: 'Belgium',
};

/**
 * Where the basket becomes an order (US-22, US-23).
 *
 * No login (US-30): a guest leaves a name, an email and an address, and the
 * order id that comes back is what they track it with.
 *
 * Submitting starts a payment rather than finishing the order, so the order id
 * is stored before the customer leaves for the provider's page (US-23).
 */
export function CheckoutPage() {
  const { basket, total, clear } = useContext(BasketContext);
  const navigate = useNavigate();
  const placeOrder = usePlaceOrder();
  const { remember } = usePlacedOrders();
  // Checked again here: this page can be reached straight from a bookmark,
  // without the basket page ever having run the check (US-21).
  const { unavailable, isChecking } = useBasketAvailability();
  const { control, handleSubmit } = useForm<CheckoutFormValues>({
    resolver: zodResolver(checkoutFormSchema),
    defaultValues: EMPTY_CHECKOUT,
  });

  if (!basket && !placeOrder.isSuccess) return <Navigate to="/basket" replace />;
  if (!basket) return null;

  const onSubmit = (values: CheckoutFormValues) => {
    placeOrder.mutate(
      {
        ...values,
        restaurantId: basket.restaurantId,
        // Ids and quantities only. The backend looks up what each dish costs,
        // so nothing here can change the price of the order.
        items: basket.lines.map((line) => ({ dishId: line.dishId, quantity: line.quantity })),
      },
      {
        onSuccess: (response) => {
          // Written down before we hand the browser over. Everything after this
          // line happens on someone else's site, and a customer who never comes
          // back would otherwise have no way to find the order again (US-24).
          remember({
            orderId: response.orderId,
            restaurantName: basket.restaurantName,
            placedAt: new Date().toISOString(),
          });
          clear();

          if (response.paymentUrl) {
            window.location.assign(response.paymentUrl);
            return;
          }

          // No payment page came back, which should not happen. Better to land
          // the customer on their order than to leave them on a dead form.
          navigate(`/orders/${response.orderId}`, { replace: true });
        },
      },
    );
  };

  return (
    <>
      <Typography variant="h1" gutterBottom>
        Checkout
      </Typography>
      <Typography color="text.secondary" gutterBottom>
        Ordering from {basket.restaurantName}
      </Typography>

      <Paper variant="outlined" sx={{ mt: 2, p: 2 }}>
        <Typography variant="h2" gutterBottom>
          Your order
        </Typography>
        <Stack spacing={1}>
          {basket.lines.map((line) => (
            <Stack key={line.dishId} direction="row" sx={{ justifyContent: 'space-between' }}>
              <Typography>
                {line.quantity} × {line.name}
              </Typography>
              <Typography>{formatPrice(line.unitPrice * line.quantity)}</Typography>
            </Stack>
          ))}
        </Stack>
        <Divider sx={{ my: 2 }} />
        <Stack direction="row" sx={{ justifyContent: 'space-between' }}>
          <Typography variant="h6">Total</Typography>
          <Typography variant="h6">{formatPrice(total)}</Typography>
        </Stack>
      </Paper>

      <Paper component="form" variant="outlined" onSubmit={handleSubmit(onSubmit)} sx={{ mt: 3, p: 2 }}>
        <Typography variant="h2" gutterBottom>
          Where should it go?
        </Typography>

        <Stack spacing={2}>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
            <FormTextField
              control={control}
              name="customerName"
              label="Your name"
              fullWidth
            />
            <FormTextField
              control={control}
              name="customerEmail"
              label="Email"
              type="email"
              fullWidth
              helperText="Where the confirmation goes"
            />
          </Stack>

          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
            <FormTextField
              control={control}
              name="deliveryStreet"
              label="Street"
              fullWidth
            />
            <FormTextField
              control={control}
              name="deliveryNumber"
              label="Number"
              sx={{ maxWidth: { sm: 140 } }}
              fullWidth
            />
          </Stack>

          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
            <FormTextField
              control={control}
              name="deliveryPostalCode"
              label="Postal code"
              sx={{ maxWidth: { sm: 160 } }}
              fullWidth
            />
            <FormTextField
              control={control}
              name="deliveryCity"
              label="City"
              fullWidth
            />
            <FormTextField
              control={control}
              name="deliveryCountry"
              label="Country"
              fullWidth
            />
          </Stack>

          {unavailable.length > 0 && (
            <Alert
              severity="warning"
              action={
                <Button color="inherit" size="small" onClick={() => navigate('/basket')}>
                  Fix my basket
                </Button>
              }
            >
              <AlertTitle>This order cannot be placed yet</AlertTitle>
              {unavailable
                .map((line) => `${line.name} (${UNAVAILABLE_LABEL[line.reason].toLowerCase()})`)
                .join(', ')}
              .
            </Alert>
          )}

          {/* Whether the kitchen is open is decided when the order lands, so
              that answer comes from there. So does a dish that sold out in the
              seconds between the check above and this button. */}
          {placeOrder.error && <Alert severity="error">{placeOrder.error.message}</Alert>}

          <Stack direction="row" spacing={2}>
            {/* Says where the button leads. The next screen is the payment
                provider's, not ours, and a button labelled "place the order"
                would make that a surprise (US-23). */}
            <Button
              type="submit"
              variant="contained"
              disabled={placeOrder.isPending || isChecking || unavailable.length > 0}
            >
              {placeOrder.isPending ? 'Taking you to payment...' : `Pay ${formatPrice(total)}`}
            </Button>
            <Button onClick={() => navigate('/basket')} disabled={placeOrder.isPending}>
              Back to the basket
            </Button>
          </Stack>
        </Stack>
      </Paper>
    </>
  );
}
