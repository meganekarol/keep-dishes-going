import { Checkbox, FormControlLabel, FormGroup } from '@mui/material';
import { Controller } from 'react-hook-form';
import type { Control } from 'react-hook-form';

import { FOOD_TAGS } from './types';
import type { DishFormValues } from './types';
import { humanise } from '../../../utils/format';

interface FoodTagFieldsProps {
  control: Control<DishFormValues>;
}

/**
 * The dietary tags customers filter on (US-13), as checkboxes.
 *
 * One Controller for the whole set rather than one per tag: the field is a
 * single array in the request, and ticking a box only adds or removes an entry.
 */
export function FoodTagFields({ control }: FoodTagFieldsProps) {
  return (
    <Controller
      control={control}
      name="foodTags"
      render={({ field }) => (
        <FormGroup row>
          {FOOD_TAGS.map((tag) => (
            <FormControlLabel
              key={tag}
              label={humanise(tag)}
              control={
                <Checkbox
                  checked={field.value.includes(tag)}
                  onChange={(event) =>
                    field.onChange(
                      event.target.checked
                        ? [...field.value, tag]
                        : field.value.filter((selected) => selected !== tag),
                    )
                  }
                />
              }
            />
          ))}
        </FormGroup>
      )}
    />
  );
}