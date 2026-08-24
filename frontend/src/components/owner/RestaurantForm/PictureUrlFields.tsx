import { Button, IconButton, Stack } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutlined';
import { useFieldArray, useFormContext } from 'react-hook-form';

import { FormTextField } from '../../form/FormTextField';
import type { RestaurantFormValues } from './types';

/**
 * The brief asks for picture(s), so the owner can add as many rows as they like.
 * The first one cannot be removed: the catalogue card shows a restaurant's first
 * picture, so a restaurant with none looks broken to customers.
 */
export function PictureUrlFields() {
  const { control } = useFormContext<RestaurantFormValues>();
  const { fields, append, remove } = useFieldArray({ control, name: 'pictureUrls' });

  return (
    <Stack spacing={2} sx={{ alignItems: 'flex-start' }}>
      {fields.map((field, index) => (
        <Stack
          key={field.id}
          direction="row"
          spacing={1}
          sx={{ width: '100%', alignItems: 'flex-start' }}
        >
          <FormTextField
            control={control}
            name={`pictureUrls.${index}.url`}
            label={index === 0 ? 'Picture URL' : `Picture URL ${index + 1}`}
            fullWidth
          />
          {index > 0 && (
            <IconButton
              aria-label="Remove this picture"
              onClick={() => remove(index)}
              sx={{ mt: 1 }}
            >
              <DeleteOutlineIcon />
            </IconButton>
          )}
        </Stack>
      ))}

      <Button startIcon={<AddIcon />} onClick={() => append({ url: '' })}>
        Add another picture
      </Button>
    </Stack>
  );
}
