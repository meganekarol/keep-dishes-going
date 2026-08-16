import { useContext } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
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

      <Paper variant="outlined" sx={{ mt: 2, p: 2 }}>
        <Stack divider={<Divider />} spacing={2}>
          {basket.lines.map((line) => (
            <Stack
              key={line.dishId}
              direction={{ xs: 'column', sm: 'row' }}
              spacing={2}
              sx={{ alignItems: { sm: 'center' } }}
            >
              <Box sx={{ flexGrow: 1 }}>
                <Typography sx={{ fontWeight: 600 }}>{line.name}</Typography>
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
                onClick={() => removeItem(line.dishId)}
              >
                <DeleteOutlineIcon />
              </IconButton>
            </Stack>
          ))}
        </Stack>

        <Divider sx={{ my: 2 }} />

        <Stack direction="row" sx={{ justifyContent: 'space-between' }}>
          <Typography variant="h6">Total</Typography>
          <Typography variant="h6">{formatPrice(total)}</Typography>
        </Stack>
      </Paper>

      <Stack direction="row" spacing={2} useFlexGap sx={{ mt: 3, flexWrap: 'wrap' }}>
        <Button variant="contained" component={RouterLink} to="/checkout">
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
