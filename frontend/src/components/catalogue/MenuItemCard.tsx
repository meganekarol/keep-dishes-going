import {
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  Chip,
  Stack,
  Typography,
} from '@mui/material';
import AddShoppingCartIcon from '@mui/icons-material/AddShoppingCart';

import { CoverImage } from '../shared/CoverImage';
import type { MenuItem } from '../../types/api';
import { formatPrice, humanise } from '../../utils/format';

interface MenuItemCardProps {
  item: MenuItem;
  /** Left out on a menu that is only being read, which then shows no button. */
  onAdd?: (item: MenuItem) => void;
  /** How many of this dish are in the basket already. */
  inBasket?: number;
}

export function MenuItemCard({ item, onAdd, inBasket = 0 }: MenuItemCardProps) {
  return (
    <Card
      sx={{
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        opacity: item.available ? 1 : 0.6,
      }}
    >
      <Box sx={{ position: 'relative' }}>
        <CoverImage src={item.pictureUrl} alt={item.name} ratio={16 / 10} />
        {!item.available && (
          <Chip
            size="small"
            color="warning"
            label="Sold out"
            sx={{ position: 'absolute', top: 12, left: 12, boxShadow: 1 }}
          />
        )}
      </Box>

      <CardContent sx={{ flexGrow: 1 }}>
        <Stack direction="row" spacing={1} sx={{ justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
            {item.name}
          </Typography>
          <Typography variant="subtitle1" sx={{ fontWeight: 600, whiteSpace: 'nowrap' }}>
            {formatPrice(item.price)}
          </Typography>
        </Stack>

        <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
          {item.description}
        </Typography>

        <Box sx={{ mt: 1.5, display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
          <Chip size="small" variant="outlined" label={humanise(item.type)} />
          {item.foodTags.map((tag) => (
            <Chip key={tag} size="small" variant="outlined" color="secondary" label={humanise(tag)} />
          ))}
        </Box>
      </CardContent>

      {onAdd && (
        <CardActions sx={{ justifyContent: 'space-between' }}>
          <Button
            size="small"
            startIcon={<AddShoppingCartIcon />}
            disabled={!item.available}
            onClick={() => onAdd(item)}
          >
            Add to basket
          </Button>
          {inBasket > 0 && <Chip size="small" color="primary" label={`${inBasket} in basket`} />}
        </CardActions>
      )}
    </Card>
  );
}
