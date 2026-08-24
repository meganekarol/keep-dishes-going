import { Alert, Box, Card, CardContent, Typography } from '@mui/material';
import { LineChart } from '@mui/x-charts/LineChart';

import { QueryState } from '../shared/QueryState';
import { usePriceRangeEvolution } from '../../hooks/usePricing';
import type { PriceRange } from '../../types/api';
import { formatPrice } from '../../utils/format';

/** The bands are ordered, so they plot as 1 to 4 and read back as symbols. */
const RANK: Record<PriceRange, number> = {
  CHEAP: 1,
  REGULAR: 2,
  EXPENSIVE: 3,
  PREMIUM: 4,
};

const SYMBOLS = ['', '€', '€€', '€€€', '€€€€'];

const LABELS: Record<PriceRange, string> = {
  CHEAP: 'cheap',
  REGULAR: 'regular',
  EXPENSIVE: 'expensive',
  PREMIUM: 'premium',
};

/**
 * How this restaurant's price range moved over time (US-26).
 */
export function PriceRangeEvolutionChart({ restaurantId }: { restaurantId: string }) {
  const { data, isLoading, error } = usePriceRangeEvolution(restaurantId);

  const points = data?.points ?? [];

  return (
    <Card>
      <CardContent sx={{ p: { xs: 2.5, md: 3 } }}>
        <Typography variant="h2" gutterBottom>
          Restaurant price range evolution
        </Typography>
        <Typography color="text.secondary" sx={{ mb: 2 }}>
          Is this place getting more expensive? € is cheap and €€€€ is pricey,
          judged against what restaurants generally charge. Hover a point to see what a dish cost
          here on average at the time.
        </Typography>

        <QueryState isLoading={isLoading} error={error}>
          {points.length === 0 ? (
            <Alert severity="info">
              Nothing to show yet. Prices here have not changed long enough to draw a line.
            </Alert>
          ) : (
            <Box sx={{ width: '100%' }}>
              <LineChart
                height={280}
                margin={{ left: 8, right: 8 }}
                xAxis={[
                  {
                    data: points.map((point) => new Date(point.at)),
                    scaleType: 'time',
                  },
                ]}
                yAxis={[
                  {
                    min: 0.5,
                    max: 4.5,
                    tickInterval: [1, 2, 3, 4],
                    valueFormatter: (value: number) => SYMBOLS[value] ?? '',
                  },
                ]}
                series={[
                  {
                    data: points.map((point) => RANK[point.priceRange]),
                    // Stepped, not smooth: a restaurant is never halfway
                    // between two bands, it is in one until the day it is not.
                    curve: 'stepAfter',
                    showMark: true,
                    label: 'Price range',
                    valueFormatter: (value, context) => {
                      const point = points[context.dataIndex];
                      if (!point) return '';
                      return `${SYMBOLS[value ?? 0]} ${LABELS[point.priceRange]}, averaging ${formatPrice(
                        point.averageMenuPrice,
                      )}`;
                    },
                  },
                ]}
              />
            </Box>
          )}
        </QueryState>
      </CardContent>
    </Card>
  );
}