import { useContext } from 'react';
import { useForm } from 'react-hook-form';
import { Navigate, useNavigate } from 'react-router-dom';
import { Alert, Button, Divider, Paper, Stack, Typography } from '@mui/material';

import { FormTextField } from '../components/form/FormTextField';
import BasketContext from '../context/BasketContext';
import { usePlaceOrder } from '../hooks/useOrderTracking';
import { usePlacedOrders } from '../hooks/usePlacedOrders';
import { formatPrice } from '../utils/format';

interface CheckoutFormValues {
  customerName: string;
  customerEmail: string;
  deliveryStreet: string;
  deliveryNumber: string;
  deliveryPostalCode: string;
  deliveryCity: string;
  deliveryCountry: string;
}

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
 * Where the basket becomes an order (US-22).
 *
 * No login (US-30): a guest leaves a name, an email and an address, and the
 * order id that comes back is what they track it with.
 */
export function CheckoutPage() {
  const { basket, total, clear } = useContext(BasketContext);
  const navigate = useNavigate();
  const placeOrder = usePlaceOrder();
  const { remember } = usePlacedOrders();
  const { control, handleSubmit } = useForm<CheckoutFormValues>({ defaultValues: EMPTY_CHECKOUT });

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
          remember({
            orderId: response.orderId,
            restaurantName: basket.restaurantName,
            placedAt: new Date().toISOString(),
          });
          clear();
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
              rules={{ required: 'We need a name for the order' }}
            />
            <FormTextField
              control={control}
              name="customerEmail"
              label="Email"
              type="email"
              fullWidth
              helperText="Where the confirmation goes"
              rules={{
                required: 'An email is required',
                pattern: { value: /^\S+@\S+\.\S+$/, message: 'That does not look like an email' },
              }}
            />
          </Stack>

          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
            <FormTextField
              control={control}
              name="deliveryStreet"
              label="Street"
              fullWidth
              rules={{ required: 'Street is required' }}
            />
            <FormTextField
              control={control}
              name="deliveryNumber"
              label="Number"
              sx={{ maxWidth: { sm: 140 } }}
              fullWidth
              rules={{ required: 'Number is required' }}
            />
          </Stack>

          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
            <FormTextField
              control={control}
              name="deliveryPostalCode"
              label="Postal code"
              sx={{ maxWidth: { sm: 160 } }}
              fullWidth
              rules={{ required: 'Postal code is required' }}
            />
            <FormTextField
              control={control}
              name="deliveryCity"
              label="City"
              fullWidth
              rules={{ required: 'City is required' }}
            />
            <FormTextField
              control={control}
              name="deliveryCountry"
              label="Country"
              fullWidth
              rules={{ required: 'Country is required' }}
            />
          </Stack>

          {/* Whether the kitchen is open and every dish still in stock is
              decided when the order lands, so that answer comes from there. */}
          {placeOrder.error && <Alert severity="error">{placeOrder.error.message}</Alert>}

          <Stack direction="row" spacing={2}>
            <Button type="submit" variant="contained" disabled={placeOrder.isPending}>
              {placeOrder.isPending ? 'Placing your order...' : 'Place the order'}
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
