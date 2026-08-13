import { Box, Card, CardContent, Chip, Stack, Typography } from '@mui/material';
import ScheduleIcon from '@mui/icons-material/Schedule';

import type { DishState, DishVersion, OwnerDishView } from '../../types/api';
import { formatDateTime, formatPrice, humanise } from '../../utils/format';

const STATUS_LABELS: Record<DishState, string> = {
  DRAFT: 'Not on the menu yet',
  PUBLISHED: 'On the menu',
  UNPUBLISHED: 'Taken off the menu',
  OUT_OF_STOCK: 'Out of stock',
};

const STATUS_COLOURS: Record<DishState, 'default' | 'success' | 'warning'> = {
  DRAFT: 'default',
  PUBLISHED: 'success',
  UNPUBLISHED: 'default',
  OUT_OF_STOCK: 'warning',
};

interface OwnerDishCardProps {
  dish: OwnerDishView;
}

/**
 * One dish on the owner's menu screen (US-03).
 *
 * When a dish has edits waiting, both versions are shown side by side. That is
 * the point of the screen: the owner has to be able to see what customers can
 * order right now next to what they have changed but not published.
 */
export function OwnerDishCard({ dish }: OwnerDishCardProps) {
  const name = dish.published?.name ?? dish.draft?.name ?? 'Untitled dish';

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', flexWrap: 'wrap', mb: 2 }}>
          <Typography variant="h2" sx={{ flexGrow: 1 }}>
            {name}
          </Typography>
          <Chip size="small" label={STATUS_LABELS[dish.status]} color={STATUS_COLOURS[dish.status]} />
          {dish.pendingChanges && <Chip size="small" color="warning" label="Changes waiting" />}
        </Stack>

        {dish.scheduledPublishAt && (
          <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
            <ScheduleIcon fontSize="small" color="action" />
            <Typography variant="body2" color="text.secondary">
              Going live on {formatDateTime(dish.scheduledPublishAt)}
            </Typography>
          </Stack>
        )}

        <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
          {dish.published && (
            <DishVersionPanel
              version={dish.published}
              label={dish.pendingChanges ? 'What customers see now' : 'On the menu'}
            />
          )}

          {/* Only when it differs from the live version: a draft edited back to
              what was already published is not a change worth showing. A dish
              that has never gone live always lands here, since a draft can never
              equal a published version that does not exist. */}
          {dish.pendingChanges && dish.draft && (
            <DishVersionPanel
              version={dish.draft}
              label={dish.published ? 'Waiting to go live' : 'Draft, not published yet'}
              highlighted
            />
          )}
        </Stack>

        {dish.lastPublishedAt && (
          <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 2 }}>
            Last published {formatDateTime(dish.lastPublishedAt)}
          </Typography>
        )}
      </CardContent>
    </Card>
  );
}

interface DishVersionPanelProps {
  version: DishVersion;
  label: string;
  highlighted?: boolean;
}

function DishVersionPanel({ version, label, highlighted }: DishVersionPanelProps) {
  return (
    <Box
      sx={{
        flex: 1,
        p: 1.5,
        border: 1,
        borderRadius: 1,
        borderColor: highlighted ? 'warning.main' : 'divider',
      }}
    >
      <Typography variant="overline" color="text.secondary">
        {label}
      </Typography>

      {version.pictureUrl && (
        <Box
          component="img"
          src={version.pictureUrl}
          alt=""
          sx={{ width: '100%', height: 120, objectFit: 'cover', borderRadius: 1, mt: 0.5 }}
        />
      )}

      <Stack direction="row" spacing={1} sx={{ justifyContent: 'space-between', alignItems: 'baseline', mt: 1 }}>
        <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
          {version.name}
        </Typography>
        <Typography variant="subtitle1" sx={{ fontWeight: 600, whiteSpace: 'nowrap' }}>
          {formatPrice(version.price)}
        </Typography>
      </Stack>

      <Typography variant="body2" color="text.secondary">
        {version.description}
      </Typography>

      <Box sx={{ mt: 1, display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
        <Chip size="small" variant="outlined" label={humanise(version.type)} />
        {version.foodTags.map((tag) => (
          <Chip key={tag} size="small" variant="outlined" color="secondary" label={humanise(tag)} />
        ))}
      </Box>
    </Box>
  );
}