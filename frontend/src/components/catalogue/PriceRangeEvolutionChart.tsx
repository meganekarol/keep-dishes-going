import { useMemo } from 'react';
import { Alert, Box, Card, CardContent, Typography } from '@mui/material';
import { LineChart } from '@mui/x-charts/LineChart';

import { QueryState } from '../shared/QueryState';
import { usePriceRangeEvolution } from '../../hooks/usePricing';
import type { PriceRange, PriceRangePoint } from '../../types/api';
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

  // One point per day, keeping the last. Every menu edit writes a point, so a
  // day of them would otherwise pile up on a single spot on the axis.
  const points = useMemo(() => {
    const byDay = new Map<string, PriceRangePoint>();
    for (const point of data?.points ?? []) byDay.set(point.at.slice(0, 10), point);
    return [...byDay.values()];
  }, [data]);

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
                    // A tick on each point rather than on round months, so the
                    // labels line up with the dots they belong to.
                    tickInterval: points.map((point) => new Date(point.at)),
                    valueFormatter: (value: Date, context) =>
                      context.location === 'tick'
                        ? `${value.getFullYear()}/${String(value.getMonth() + 1).padStart(2, '0')}`
                        : value.toLocaleDateString('en-GB', {
                            day: 'numeric',
                            month: 'long',
                            year: 'numeric',
                          }),
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
                    // Straight lines from point to point
                    curve: 'linear',
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