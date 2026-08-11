import { Controller } from 'react-hook-form';
import type { Control, FieldPath, FieldValues, RegisterOptions } from 'react-hook-form';
import { TextField } from '@mui/material';
import type { TextFieldProps } from '@mui/material';

type FormTextFieldProps<T extends FieldValues> = Omit<
  TextFieldProps,
  'name' | 'value' | 'onChange' | 'onBlur' | 'error'
> & {
  name: FieldPath<T>;
  control: Control<T>;
  rules?: RegisterOptions<T, FieldPath<T>>;
};

/**
 * An MUI text field bound to React Hook Form through a Controller.
 *
 * Controller rather than register because MUI does not hand the ref of its
 * underlying input to whoever spreads props onto it, which is the case the
 * course slides call out. Wrapping it once keeps that decision in one file
 * instead of repeating it per field.
 */
export function FormTextField<T extends FieldValues>({
  name,
  control,
  rules,
  helperText,
  ...textFieldProps
}: FormTextFieldProps<T>) {
  return (
    <Controller
      name={name}
      control={control}
      rules={rules}
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