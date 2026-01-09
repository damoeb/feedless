import { FormControl, Validators } from '@angular/forms';

export function createEmailFormControl<T extends string>(defaultValue: T): FormControl<T> {
  return new FormControl<T>(defaultValue, [Validators.required, Validators.email]);
}
