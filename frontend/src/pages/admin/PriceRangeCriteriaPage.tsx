import { useForm } from 'react-hook-form';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';

import { FormTextField } from '../../components/form/FormTextField';
import { QueryState } from '../../components/shared/QueryState';
import { useAdjustPriceRangeCriteria, usePriceRangeCriteria } from '../../hooks/usePricing';
import { formatPrice } from '../../utils/format';

interface CriteriaFormValues {
  effectiveFrom: string;
  cheapUnder: string;
  regularUnder: string;
  expensiveUnder: string;
}

/** The thresholds the platform launched with, offered as a starting point. */
const DEFAULTS: CriteriaFormValues = {
  effectiveFrom: '',
  cheapUnder: '10',
  regularUnder: '30',
  expensiveUnder: '60',
};

/**
 * The platform admin's price range criteria (US-32).
 */
export function PriceRangeCriteriaPage() {
  const { data, isLoading, error } = usePriceRangeCriteria();
  const adjust = useAdjustPriceRangeCriteria();
  const { control, handleSubmit, reset } = useForm<CriteriaFormValues>({ defaultValues: DEFAULTS });

  const onSubmit = (values: CriteriaFormValues) => {
    adjust.mutate(
      {
        effectiveFrom: values.effectiveFrom,
        cheapUnder: Number(values.cheapUnder),
        regularUnder: Number(values.regularUnder),
        expensiveUnder: Number(values.expensiveUnder),
      },
      { onSuccess: () => reset(DEFAULTS) },
    );
  };

  return (
    <>
      <Typography variant="h1" gutterBottom>
        Price ranges
      </Typography>
      <Typography color="text.secondary" sx={{ mb: 3 }}>
        What the platform counts as cheap, regular, expensive and premium, measured on a
        restaurant's average menu price.
      </Typography>

      <Card component="form" onSubmit={handleSubmit(onSubmit)} sx={{ mb: 4 }}>
        <CardContent sx={{ p: { xs: 2.5, md: 3 } }}>
          <Typography variant="h2" gutterBottom>
            Set new thresholds
          </Typography>
    
          <Typography color="text.secondary" sx={{ mb: 2 }}>
            The date may be in the past. Every restaurant's price history is recalculated against
            whichever thresholds were in force at the time.
          </Typography>

          <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ alignItems: 'flex-start' }}>
            <FormTextField
              control={control}
              name="effectiveFrom"
              label="In force from"
              type="date"
              slotProps={{ inputLabel: { shrink: true } }}
              rules={{ required: 'A date is required' }}
              sx={{ minWidth: 190 }}
            />
            <FormTextField
              control={control}
              name="cheapUnder"
              label="Cheap under"
              type="number"
              slotProps={{ htmlInput: { min: 0, step: '0.01' } }}
              rules={{ required: 'Required' }}
              sx={{ maxWidth: 150 }}
            />
            <FormTextField
              control={control}
              name="regularUnder"
              label="Regular under"
              type="number"
              slotProps={{ htmlInput: { min: 0, step: '0.01' } }}
              rules={{ required: 'Required' }}
              sx={{ maxWidth: 150 }}
            />
            <FormTextField
              control={control}
              name="expensiveUnder"
              label="Expensive under"
              type="number"
              slotProps={{ htmlInput: { min: 0, step: '0.01' } }}
              rules={{ required: 'Required' }}
              sx={{ maxWidth: 170 }}
            />
          </Stack>

          {adjust.error && (
            <Alert severity="error" sx={{ mt: 2 }}>
              {adjust.error.message}
            </Alert>
          )}

          <Box sx={{ mt: 3 }}>
            <Button type="submit" variant="contained" disabled={adjust.isPending}>
              {adjust.isPending ? 'Saving...' : 'Apply these thresholds'}
            </Button>
          </Box>
        </CardContent>
      </Card>

      <Typography variant="h2" gutterBottom>
        History
      </Typography>

      <QueryState isLoading={isLoading} error={error}>
        {data?.length === 0 ? (
          <Alert severity="info">
            No thresholds set yet, so the platform is using its starting values: cheap under{' '}
            {formatPrice(10)}, regular under {formatPrice(30)}, expensive under {formatPrice(60)}.
          </Alert>
        ) : (
          <Card>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>In force from</TableCell>
                  <TableCell align="right">€ under</TableCell>
                  <TableCell align="right">€€ under</TableCell>
                  <TableCell align="right">€€€ under</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {data?.map((version) => (
                  <TableRow key={version.effectiveFrom}>
                    <TableCell>
                      {new Date(version.effectiveFrom).toLocaleDateString('en-GB', {
                        day: 'numeric',
                        month: 'long',
                        year: 'numeric',
                      })}
                    </TableCell>
                    <TableCell align="right">{formatPrice(version.cheapUnder)}</TableCell>
                    <TableCell align="right">{formatPrice(version.regularUnder)}</TableCell>
                    <TableCell align="right">{formatPrice(version.expensiveUnder)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Card>
        )}
      </QueryState>
    </>
  );
}