import {
  Component,
  computed,
  effect,
  inject,
  input,
  output,
  PLATFORM_ID,
  ChangeDetectionStrategy
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import {
  IonButton,
  IonCheckbox,
  IonInput,
  IonItem,
  IonLabel,
  IonList,
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { sendOutline } from 'ionicons/icons';
import { createEmailFormControl } from '@feedless/core';
// eslint-disable-next-line @nx/enforce-module-boundaries
import { IconComponent } from '@feedless/components';
import { ReportSubscriptionValue } from './report-subscription';

/**
 * A subscription's form - just the fields, their rules, and the value it
 * emits on submit.
 *
 * It calls no service and knows nothing of the modal it currently sits in,
 * so it can later be embedded on a page in the account area just the same.
 * Given an initial value, it edits an existing subscription: then the terms
 * checkbox is dropped, since it was already accepted at creation.
 */
@Component({
  selector: 'app-report-subscription-form',
  templateUrl: './report-subscription-form.component.html',
  styleUrls: ['./report-subscription-form.component.scss'],
  imports: [
    ReactiveFormsModule,
    IonList,
    IonItem,
    IonInput,
    IonCheckbox,
    IonLabel,
    IonButton,
    IconComponent,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: true,
})
export class ReportSubscriptionFormComponent {
  private readonly platformId = inject(PLATFORM_ID);

  /** An existing subscription. Without an initial value the form creates one. */
  readonly initial = input<ReportSubscriptionValue | null>(null);
  readonly submitted = output<ReportSubscriptionValue>();

  protected readonly isEdit = computed(() => this.initial() != null);

  readonly form = new FormGroup({
    name: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(3)],
    }),
    email: createEmailFormControl<string>(''),
    acceptedTerms: new FormControl(false, { nonNullable: true }),
  });

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({ sendOutline });
    }

    effect(() => {
      const initial = this.initial();
      const terms = this.form.controls.acceptedTerms;
      if (initial) {
        this.form.patchValue({ name: initial.name, email: initial.email });
        terms.clearValidators();
      } else {
        terms.setValidators(Validators.requiredTrue);
      }
      terms.updateValueAndValidity();
    });
  }

  submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      return;
    }
    const { name, email } = this.form.getRawValue();
    this.submitted.emit({ name, email });
  }
}
