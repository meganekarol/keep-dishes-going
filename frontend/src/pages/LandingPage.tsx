import { Box, Button, Card, CardContent, Paper, Stack, Typography } from '@mui/material';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import RestaurantMenuIcon from '@mui/icons-material/RestaurantMenu';
import StorefrontIcon from '@mui/icons-material/Storefront';
import { Link } from 'react-router-dom';
import { useContext, type ReactNode } from 'react';

import { CoverImage } from '../components/shared/CoverImage';
import SecurityContext from '../context/SecurityContext';
import { useRestaurants } from '../hooks/useRestaurants';

const TILT = ['rotate(-2deg)', 'rotate(1.5deg)', 'rotate(-1deg)'];

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

  const { data: restaurants } = useRestaurants();
  const showcase = (restaurants ?? []).slice(0, 3);

  return (
    <Stack spacing={5} sx={{ py: 2 }}>
      <Box
        sx={{
          overflow: 'hidden',
          borderRadius: 4,
          color: 'common.white',
          background: (theme) =>
            `linear-gradient(135deg, ${theme.palette.primary.dark} 0%, ${theme.palette.primary.main} 100%)`,
          px: { xs: 3, md: 6 },
          py: { xs: 5, md: 7 },
          display: 'grid',
          gridTemplateColumns: { xs: '1fr', md: showcase.length > 0 ? '1fr 1fr' : '1fr' },
          alignItems: 'center',
          gap: { xs: 4, md: 6 },
        }}
      >
        <Box sx={{ maxWidth: 620 }}>
          <Typography variant="overline" sx={{ opacity: 0.85 }}>
            Order in from kitchens nearby
          </Typography>
          <Typography variant="h1" sx={{ mt: 1, fontSize: { xs: '2.25rem', md: '3.25rem' } }}>
            Good food, still going.
          </Typography>
          <Typography sx={{ mt: 2, opacity: 0.88, fontSize: '1.1rem', maxWidth: '48ch' }}>
            Browse the restaurants around you, build an order and follow it all the way to your
            door. No account needed.
          </Typography>
          <Button
            variant="contained"
            size="large"
            component={Link}
            to="/restaurants"
            endIcon={<ArrowForwardIcon />}
            sx={{
              mt: 4,
              bgcolor: 'common.white',
              color: 'primary.dark',
              '&:hover': { bgcolor: 'common.white', filter: 'brightness(0.94)' },
            }}
          >
            Find a restaurant
          </Button>
        </Box>

        {showcase.length > 0 && (
          <Box
            aria-hidden
            sx={{
              display: { xs: 'none', md: 'grid' },
              '& > :first-of-type': { gridColumn: '1 / -1' },
            }}
          >
            {showcase.map((restaurant, index) => (
              <Paper
                key={restaurant.restaurantId}
                elevation={0}
                sx={{
                  position: 'relative',
                  overflow: 'hidden',
                  borderRadius: 3,
                  transform: TILT[index],
                  boxShadow: '0 22px 44px rgba(28,25,23,.35)',
                  '@media (prefers-reduced-motion: reduce)': { transform: 'none' },
                }}
              >
                <CoverImage
                  src={restaurant.pictureUrls[0]}
                  ratio={index === 0 ? 16 / 9 : 4 / 3}
                  placeholderTone="dark"
                />
                <Box
                  sx={{
                    position: 'absolute',
                    inset: 10,
                    border: '1px solid rgba(255,255,255,.3)',
                    borderRadius: 2,
                    pointerEvents: 'none',
                  }}
                />
              </Paper>
            ))}
          </Box>
        )}
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