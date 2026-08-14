import { Alert, Chip, Paper, Stack, Typography } from '@mui/material';

import type { OwnerMenu } from '../../types/api';

interface MenuSummaryBarProps {
  menu: OwnerMenu;
}

/**
 * The two numbers an owner runs their menu by (US-03, US-27).
 *
 * The cap comes from the backend rather than being written in here, so the
 * figure on screen is the one the next publish will actually be checked against.
 */
export function MenuSummaryBar({ menu }: MenuSummaryBarProps) {
  const full = menu.publishedCount >= menu.maxPublishedDishes;

  return (
    <Stack spacing={2}>
      <Paper variant="outlined" elevation={0} sx={{ p: 2 }}>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ alignItems: { sm: 'center' } }}>
          <Typography variant="body2" sx={{ flexGrow: 1 }}>
            {menu.publishedCount} of {menu.maxPublishedDishes} dishes are on the menu.
          </Typography>
          <Chip
            size="small"
            color={full ? 'warning' : 'default'}
            label={`${menu.dishes.length} in total`}
          />
        </Stack>
      </Paper>

      {full && (
        <Alert severity="info">
          Your menu is full. Take a dish off the menu before putting another one live.
        </Alert>
      )}
    </Stack>
  );
}