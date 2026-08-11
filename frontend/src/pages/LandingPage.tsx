import { Box, Button, Card, CardContent, Stack, Typography } from '@mui/material';
import RestaurantMenuIcon from '@mui/icons-material/RestaurantMenu';
import StorefrontIcon from '@mui/icons-material/Storefront';
import { Link } from 'react-router-dom';
import type { ReactNode } from 'react';

interface ChoiceCardProps {
  icon: ReactNode;
  title: string;
  description: string;
  actionLabel: string;
  to: string;
}

function ChoiceCard({ icon, title, description, actionLabel, to }: ChoiceCardProps) {
  return (
    <Card variant="outlined" sx={{ flex: 1, display: 'flex' }}>
      <CardContent sx={{ display: 'flex', flexDirection: 'column', gap: 1.5, p: 3, flex: 1 }}>
        {icon}
        <Typography variant="h2">{title}</Typography>
        <Typography color="text.secondary" sx={{ flexGrow: 1 }}>
          {description}
        </Typography>
        <Box>
          <Button variant="contained" size="large" component={Link} to={to}>
            {actionLabel}
          </Button>
        </Box>
      </CardContent>
    </Card>
  );
}

/**
 * The entry point, offering the choice: continue as an owner
 * or as a customer (US-13).
 *
 * The owner card links straight to the management area and lets the route guard
 * there start the sign-in.
 */
export function LandingPage() {
  return (
    <Stack spacing={4} sx={{ py: 4 }}>
      <Box>
        <Typography variant="h1" gutterBottom>
          Keep Dishes Going
        </Typography>
        <Typography color="text.secondary">
          Order from restaurants near you, or manage the one you run.
        </Typography>
      </Box>

      <Stack direction={{ xs: 'column', md: 'row' }} spacing={3}>
        <ChoiceCard
          icon={<RestaurantMenuIcon color="primary" fontSize="large" />}
          title="I am hungry"
          description="Browse restaurants, build an order and follow it to your door. No account needed."
          actionLabel="Continue as customer"
          to="/restaurants"
        />
        <ChoiceCard
          icon={<StorefrontIcon color="secondary" fontSize="large" />}
          title="I run a restaurant"
          description="Sign in to set up your restaurant, manage your menu and decide on incoming orders."
          actionLabel="Continue as owner"
          to="/owner"
        />
      </Stack>
    </Stack>
  );
}