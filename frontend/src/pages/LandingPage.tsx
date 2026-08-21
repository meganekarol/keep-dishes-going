import { Box, Button, Card, CardContent, Stack, Typography } from '@mui/material';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import RestaurantMenuIcon from '@mui/icons-material/RestaurantMenu';
import StorefrontIcon from '@mui/icons-material/Storefront';
import { Link } from 'react-router-dom';
import { useContext, type ReactNode } from 'react';

import SecurityContext from '../context/SecurityContext';

interface ChoiceCardProps {
  icon: ReactNode;
  tint: 'primary' | 'secondary';
  title: string;
  description: string;
  actionLabel: string;
  to: string;
  secondaryAction?: ReactNode;
}

function ChoiceCard({
  icon,
  tint,
  title,
  description,
  actionLabel,
  to,
  secondaryAction,
}: ChoiceCardProps) {
  return (
    <Card sx={{ flex: 1, display: 'flex' }}>
      <CardContent sx={{ display: 'flex', flexDirection: 'column', gap: 1.5, p: 3, flex: 1 }}>
        <Box
          sx={{
            width: 48,
            height: 48,
            borderRadius: '50%',
            bgcolor: `${tint}.light`,
            color: `${tint}.main`,
            display: 'grid',
            placeItems: 'center',
          }}
        >
          {icon}
        </Box>
        <Typography variant="h2">{title}</Typography>
        <Typography color="text.secondary" sx={{ flexGrow: 1 }}>
          {description}
        </Typography>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mt: 1 }}>
          <Button variant="contained" size="large" component={Link} to={to}>
            {actionLabel}
          </Button>
          {secondaryAction}
        </Stack>
      </CardContent>
    </Card>
  );
}

/**
 * The entry point, offering the choice: continue as an owner
 * or as a customer (US-13).
 */
export function LandingPage() {
  const { isAuthenticated, register } = useContext(SecurityContext);
  const signedIn = isAuthenticated();

  return (
    <Stack spacing={5} sx={{ py: 2 }}>
      <Box
        sx={{
          position: 'relative',
          overflow: 'hidden',
          borderRadius: 4,
          bgcolor: 'primary.light',
          px: { xs: 3, md: 6 },
          py: { xs: 5, md: 8 },
        }}
      >
        <Box sx={{ position: 'relative', zIndex: 1, maxWidth: 620 }}>
          <Typography variant="overline" sx={{ color: 'primary.dark' }}>
            Order in from kitchens nearby
          </Typography>
          <Typography variant="h1" sx={{ mt: 1, fontSize: { xs: '2.25rem', md: '3.25rem' } }}>
            Good food, still going.
          </Typography>
          <Typography sx={{ mt: 2, color: 'text.secondary', fontSize: '1.1rem', maxWidth: '48ch' }}>
            Browse the restaurants around you, build an order and follow it all the way to your
            door. No account needed.
          </Typography>
          <Button
            variant="contained"
            size="large"
            component={Link}
            to="/restaurants"
            endIcon={<ArrowForwardIcon />}
            sx={{ mt: 4 }}
          >
            Find a restaurant
          </Button>
        </Box>

        {/* Decoration, so it is hidden from assistive tech and from narrow
            screens where it would crowd the words. */}
        <RestaurantMenuIcon
          aria-hidden
          sx={{
            position: 'absolute',
            right: -40,
            bottom: -70,
            fontSize: 320,
            color: 'primary.main',
            opacity: 0.09,
            display: { xs: 'none', md: 'block' },
          }}
        />
      </Box>

      <Stack direction={{ xs: 'column', md: 'row' }} spacing={3}>
        <ChoiceCard
          icon={<RestaurantMenuIcon />}
          tint="primary"
          title="I am hungry"
          description="Browse restaurants, build an order and follow it to your door. No account needed."
          actionLabel="Continue as customer"
          to="/restaurants"
        />
        <ChoiceCard
          icon={<StorefrontIcon />}
          tint="secondary"
          title="I run a restaurant"
          description="Set up your restaurant, manage your menu and decide on incoming orders."
          actionLabel={signedIn ? 'Go to my restaurant' : 'Sign in as owner'}
          to="/owner"
          secondaryAction={
            signedIn ? undefined : <Button onClick={register}>Create an account</Button>
          }
        />
      </Stack>
    </Stack>
  );
}