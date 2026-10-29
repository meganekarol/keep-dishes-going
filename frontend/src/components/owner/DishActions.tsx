import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { Alert, Button, Stack, Tooltip } from '@mui/material';
import AddShoppingCartIcon from '@mui/icons-material/AddShoppingCart';
import EditIcon from '@mui/icons-material/Edit';
import PublishIcon from '@mui/icons-material/Publish';
import RemoveShoppingCartIcon from '@mui/icons-material/RemoveShoppingCart';
import VisibilityOffIcon from '@mui/icons-material/VisibilityOff';

import {
  useMarkDishInStock,
  useMarkDishOutOfStock,
  usePublishDish,
  useUnpublishDish,
} from '../../hooks/useDishActions';
import type { OwnerDishView } from '../../types/api';

interface DishActionsProps {
  dish: OwnerDishView;
  restaurantId: string;
  /** Publishing a dish that is not already on the menu is refused while full. */
  menuIsFull: boolean;
}

/**
 * What an owner can do to one dish (US-04, US-05, US-08).
 *
 * Every button mirrors a rule the backend enforces, so a refused action is
 * disabled with the reason on it rather than left to fail on the server. The
 * checks read off the same two facts the domain uses: whether a draft exists,
 * and whether the dish is currently on the menu.
 */
export function DishActions({ dish, restaurantId, menuIsFull }: DishActionsProps) {
  const publish = usePublishDish(restaurantId);
  const unpublish = useUnpublishDish(restaurantId);
  const markOutOfStock = useMarkDishOutOfStock(restaurantId);
  const markInStock = useMarkDishInStock(restaurantId);

  const mutations = [publish, unpublish, markOutOfStock, markInStock];
  const busy = mutations.some((mutation) => mutation.isPending);
  const failure = mutations.find((mutation) => mutation.error)?.error;

  // Out of stock keeps a dish on the menu, greyed out, so it still holds a slot.
  const onMenu = dish.status === 'PUBLISHED' || dish.status === 'OUT_OF_STOCK';
  const hasDraft = dish.draft !== null;

  return (
    <Stack spacing={1} sx={{ width: '100%' }}>
      {/* useFlexGap, so the buttons keep their spacing once they wrap onto a
          second line on a narrow screen. */}
      <Stack direction="row" spacing={1} useFlexGap sx={{ flexWrap: 'wrap' }}>
        <Button
          size="small"
          startIcon={<EditIcon />}
          component={Link}
          to={`/owner/menu/${dish.dishId}/edit`}
        >
          Edit
        </Button>

        {/* Hidden for a dish that is already live with nothing waiting: there
            would be nothing for it to do. */}
        {(hasDraft || !onMenu) && (
          <ActionButton
            label={onMenu ? 'Publish changes' : dish.published ? 'Put back on the menu' : 'Put on the menu'}
            icon={<PublishIcon />}
            busy={busy}
            onClick={() => publish.mutate(dish.dishId)}
            disabledReason={
              !hasDraft && !dish.published
                ? 'There is nothing to publish yet.'
                : !onMenu && menuIsFull
                  ? 'Your menu is full. Take another dish off it first.'
                  : undefined
            }
          />
        )}

        {onMenu && (
          <ActionButton
            label="Take off the menu"
            icon={<VisibilityOffIcon />}
            busy={busy}
            onClick={() => unpublish.mutate(dish.dishId)}
            disabledReason={hasDraft ? 'Publish the waiting changes first.' : undefined}
            enabledHint="Customers stop seeing it. You can put it back whenever you like."
          />
        )}

        {dish.status === 'PUBLISHED' && (
          <ActionButton
            label="Mark sold out"
            icon={<RemoveShoppingCartIcon />}
            busy={busy}
            onClick={() => markOutOfStock.mutate(dish.dishId)}
            enabledHint="It stays on the menu, marked unavailable."
          />
        )}

        {dish.status === 'OUT_OF_STOCK' && (
          <ActionButton
            label="Back in stock"
            icon={<AddShoppingCartIcon />}
            busy={busy}
            onClick={() => markInStock.mutate(dish.dishId)}
          />
        )}
      </Stack>

      {failure instanceof Error && <Alert severity="error">{failure.message}</Alert>}
    </Stack>
  );
}

interface ActionButtonProps {
  label: string;
  icon: ReactNode;
  onClick: () => void;
  busy: boolean;
  /** Why the button is off. Its presence is what disables the button. */
  disabledReason?: string;
  /** Shown while the button still works, for actions worth a warning. */
  enabledHint?: string;
}

function ActionButton({ label, icon, onClick, busy, disabledReason, enabledHint }: ActionButtonProps) {
  const disabled = Boolean(disabledReason);

  const button = (
    <Button size="small" startIcon={icon} onClick={onClick} disabled={disabled || busy}>
      {label}
    </Button>
  );

  const tooltip = disabledReason ?? enabledHint;
  if (!tooltip) return button;

  // A disabled button takes no pointer events of its own, so the tooltip listens
  // on a wrapper instead. Without it the explanation never appears.
  return (
    <Tooltip title={tooltip}>
      <span>{button}</span>
    </Tooltip>
  );
}
