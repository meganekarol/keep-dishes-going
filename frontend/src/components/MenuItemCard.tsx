import { Box, Card, CardContent, CardMedia, Chip, Stack, Typography } from '@mui/material';

import type { MenuItem } from '../types/api';
import { formatPrice, humanise } from '../utils/format';

/**
 * Out-of-stock dishes stay on the menu with `available: false` rather than
 * disappearing — a customer who came for one deserves to see it is temporarily
 * off, not to wonder whether they misremembered.
 */
export function MenuItemCard({ item }: { item: MenuItem }) {
  return (
    <Card sx={{ height: '100%', opacity: item.available ? 1 : 0.6 }}>
      {item.pictureUrl && <CardMedia component="img" height="140" image={item.pictureUrl} alt="" />}
      <CardContent>
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
          {!item.available && <Chip size="small" color="warning" label="Out of stock" />}
        </Box>
      </CardContent>
    </Card>
  );
}
