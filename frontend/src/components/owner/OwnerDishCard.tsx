import { Box, Card, CardActions, CardContent, Chip, Stack, Typography } from '@mui/material';
import ScheduleIcon from '@mui/icons-material/Schedule';

import { DishActions } from './DishActions';
import { CoverImage } from '../shared/CoverImage';
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
  restaurantId: string;
  menuIsFull: boolean;
}

/**
 * One dish on the owner's menu screen (US-03).
 *
 * When a dish has edits waiting, both versions are shown side by side. That is
 * the point of the screen: the owner has to be able to see what customers can
 * order right now next to what they have changed but not published.
 */
export function OwnerDishCard({ dish, restaurantId, menuIsFull }: OwnerDishCardProps) {
  const name = dish.published?.name ?? dish.draft?.name ?? 'Untitled dish';

  // Two panels only when there is a live version and a changed one to hold
  // against it. On a single panel the card heading already says the name and
  // the status, so repeating them inside is noise.
  const comparing = Boolean(dish.published && dish.pendingChanges && dish.draft);

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
              label={comparing ? 'What customers see now' : undefined}
            />
          )}

          {/* Only when it differs from the live version: a draft edited back to
              what was already published is not a change worth showing. A dish
              that has never gone live always lands here, since a draft can never
              equal a published version that does not exist. */}
          {dish.pendingChanges && dish.draft && (
            <DishVersionPanel
              version={dish.draft}
              label={comparing ? 'Waiting to go live' : undefined}
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

      <CardActions sx={{ px: 2, pb: 2, pt: 0 }}>
        <DishActions dish={dish} restaurantId={restaurantId} menuIsFull={menuIsFull} />
      </CardActions>
    </Card>
  );
}

interface DishVersionPanelProps {
  version: DishVersion;
  /** Left out on a lone panel, where the card heading already says as much. */
  label?: string;
  highlighted?: boolean;
}

function DishVersionPanel({ version, label, highlighted }: DishVersionPanelProps) {
  return (
    <Box
      sx={{
        flex: 1,
        minWidth: 0,
        p: 1.5,
        border: 1,
        borderRadius: 2,
        borderColor: highlighted ? 'warning.main' : 'divider',
        bgcolor: highlighted ? 'warning.light' : 'transparent',
      }}
    >
      {label && (
        <Typography variant="overline" color="text.secondary" sx={{ display: 'block', mb: 0.5 }}>
          {label}
        </Typography>
      )}

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
        {/* A fixed width rather than a fixed height: the panel is full width on
            its own and half of one when compared, and only the width changing
            keeps the picture the same shape in both. */}
        <Box sx={{ width: { xs: '100%', sm: 168 }, flexShrink: 0, borderRadius: 2, overflow: 'hidden' }}>
          <CoverImage src={version.pictureUrl} alt={version.name} ratio={4 / 3} />
        </Box>

        <Box sx={{ minWidth: 0, flex: 1 }}>
          <Stack direction="row" spacing={1} sx={{ justifyContent: 'space-between', alignItems: 'baseline' }}>
            <Typography variant="subtitle1">{version.name}</Typography>
            <Typography variant="subtitle1" sx={{ whiteSpace: 'nowrap' }}>
              {formatPrice(version.price)}
            </Typography>
          </Stack>

          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
            {version.description}
          </Typography>

          <Box sx={{ mt: 1.5, display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
            <Chip size="small" variant="outlined" label={humanise(version.type)} />
            {version.foodTags.map((tag) => (
              <Chip key={tag} size="small" variant="outlined" color="secondary" label={humanise(tag)} />
            ))}
          </Box>
        </Box>
      </Stack>
    </Box>
  );
}