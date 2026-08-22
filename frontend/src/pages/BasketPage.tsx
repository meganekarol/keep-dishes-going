import { useContext } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import {
  Alert,
  AlertTitle,
  Box,
  Button,
  Chip,
  Divider,
  IconButton,
  Link,
  Paper,
  Stack,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutlined';
import RemoveIcon from '@mui/icons-material/Remove';

import BasketContext from '../context/BasketContext';
import { UNAVAILABLE_LABEL, useBasketAvailability } from '../hooks/useBasketAvailability';
import { formatPrice } from '../utils/format';

/**
 * Review and adjust the basket before ordering (US-21).
 *
 * Prices here are the ones the menu showed. The backend prices the order again
 * when it is placed, so this is what the customer expects to pay rather than a
 * promise.
 */
export function BasketPage() {
  const { basket, total, setQuantity, removeItem, clear } = useContext(BasketContext);
  const { unavailable, isChecking } = useBasketAvailability();

  const reasonFor = (dishId: string) =>
    unavailable.find((line) => line.dishId === dishId)?.reason;

  if (!basket) {
    return (
      <>
        <Typography variant="h1" gutterBottom>
          Your basket
        </Typography>
        <Alert severity="info" sx={{ mb: 2 }}>
          There is nothing in your basket yet.
        </Alert>
        <Button variant="contained" component={RouterLink} to="/restaurants">
          Find a restaurant
        </Button>
      </>
    );
  }

  return (
    <>
      <Typography variant="h1" gutterBottom>
        Your basket
      </Typography>
      <Typography color="text.secondary" gutterBottom>
        Ordering from{' '}
        <Link component={RouterLink} to={`/restaurants/${basket.restaurantId}`}>
          {basket.restaurantName}
        </Link>
      </Typography>

      {/* The requirement is that the basket itself makes this obvious (US-21),
          so it is said here in full rather than left to the per-line chips. */}
      {unavailable.length > 0 && (
        <Alert
          severity="warning"
          sx={{ mt: 2 }}
          action={
            <Button
              color="inherit"
              size="small"
              onClick={() => unavailable.forEach((line) => removeItem(line.dishId))}
            >
              Remove {unavailable.length === 1 ? 'it' : 'them'}
            </Button>
          }
        >
          <AlertTitle>Your basket needs a change</AlertTitle>
          {unavailable
            .map((line) => `${line.name} (${UNAVAILABLE_LABEL[line.reason].toLowerCase()})`)
            .join(', ')}
          . Checkout stays closed until {unavailable.length === 1 ? 'it is' : 'they are'} removed.
        </Alert>
      )}

      <Paper variant="outlined" sx={{ mt: 2, p: 2 }}>
        <Stack divider={<Divider />} spacing={2}>
          {basket.lines.map((line) => {
            const reason = reasonFor(line.dishId);

            return (
              <Stack
                key={line.dishId}
                direction={{ xs: 'column', sm: 'row' }}
                spacing={2}
                sx={{ alignItems: { sm: 'center' } }}
              >
                {/* Dimmed here rather than on the whole row: the remove button
                    is what fixes this, and fading it would hide the way out. */}
                <Box sx={{ flexGrow: 1, opacity: reason ? 0.6 : 1 }}>
                  <Stack direction="row" spacing={1} sx={{ alignItems: 'center', flexWrap: 'wrap' }}>
                    <Typography sx={{ fontWeight: 600 }}>{line.name}</Typography>
                    {reason && (
                      <Chip size="small" color="warning" label={UNAVAILABLE_LABEL[reason]} />
                    )}
                  </Stack>
                  <Typography variant="body2" color="text.secondary">
                    {formatPrice(line.unitPrice)} each
                  </Typography>
                </Box>

                <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                  <IconButton
                    size="small"
                    aria-label={`One fewer ${line.name}`}
                    onClick={() => setQuantity(line.dishId, line.quantity - 1)}
                  >
                    <RemoveIcon fontSize="small" />
                  </IconButton>
                  <Typography sx={{ minWidth: 24, textAlign: 'center' }}>{line.quantity}</Typography>
                  <IconButton
                    size="small"
                    aria-label={`One more ${line.name}`}
                    onClick={() => setQuantity(line.dishId, line.quantity + 1)}
                  >
                    <AddIcon fontSize="small" />
                  </IconButton>
                </Stack>

                <Typography sx={{ minWidth: 88, textAlign: 'right', fontWeight: 600 }}>
                  {formatPrice(line.unitPrice * line.quantity)}
                </Typography>

                <IconButton
                  aria-label={`Remove ${line.name}`}
                  color={reason ? 'warning' : 'default'}
                  onClick={() => removeItem(line.dishId)}
                >
                  <DeleteOutlineIcon />
                </IconButton>
              </Stack>
            );
          })}
        </Stack>

        <Divider sx={{ my: 2 }} />

        <Stack direction="row" sx={{ justifyContent: 'space-between' }}>
          <Typography variant="h6">Total</Typography>
          <Typography variant="h6">{formatPrice(total)}</Typography>
        </Stack>
      </Paper>

      <Stack direction="row" spacing={2} useFlexGap sx={{ mt: 3, flexWrap: 'wrap' }}>
        {/* Blocked, not merely warned about (US-21). Also while the check is
            still running: an unknown answer is not a yes. */}
        <Button
          variant="contained"
          component={RouterLink}
          to="/checkout"
          disabled={isChecking || unavailable.length > 0}
        >
          Go to checkout
        </Button>
        <Button component={RouterLink} to={`/restaurants/${basket.restaurantId}`}>
          Add more dishes
        </Button>
        <Button color="error" onClick={clear}>
          Empty the basket
        </Button>
      </Stack>
    </>
  );
}
