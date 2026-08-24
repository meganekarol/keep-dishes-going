import { Controller } from 'react-hook-form';
import type { Control, FieldPath, FieldValues } from 'react-hook-form';
import { TextField } from '@mui/material';
import type { TextFieldProps } from '@mui/material';

type FormTextFieldProps<T extends FieldValues> = Omit<
  TextFieldProps,
  'name' | 'value' | 'onChange' | 'onBlur' | 'error'
> & {
  name: FieldPath<T>;
  /** Omit it inside a FormProvider, where Controller finds the form itself. */
  control?: Control<T>;
};

/**
 * An MUI text field bound to React Hook Form through a Controller.
 *
 * Controller rather than register because MUI does not hand the ref of its
 * underlying input to whoever spreads props onto it, which is the case the
 * course slides call out. Wrapping it once keeps that decision in one file
 * instead of repeating it per field.
 *
 * It takes no validation rules. Every form here validates through a Zod schema,
 * so the rules live in one place rather than beside each input.
 */
export function FormTextField<T extends FieldValues>({
  name,
  control,
  helperText,
  ...textFieldProps
}: FormTextFieldProps<T>) {
  return (
    <Controller
      name={name}
      control={control}
      render={({ field, fieldState }) => (
        <TextField
          {...textFieldProps}
          {...field}
          error={Boolean(fieldState.error)}
          helperText={fieldState.error?.message ?? helperText}
        />
      )}
    />
  );
}