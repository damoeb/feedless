import {
  Component,
  computed,
  effect,
  inject,
  input,
  output,
  PLATFORM_ID,
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
 * Das Formular eines Abos - nur die Felder, ihre Regeln und der Wert, den es
 * beim Absenden herausgibt.
 *
 * Es ruft keinen Service auf und weiss nichts von dem Modal, in dem es heute
 * steckt. Damit lässt es sich später ebenso auf einer Seite im Account-Bereich
 * einhängen. Bekommt es einen Ausgangswert, bearbeitet es ein bestehendes Abo:
 * dann entfällt das AGB-Häkchen, weil es beim Anlegen bereits bestätigt wurde.
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
  standalone: true,
})
export class ReportSubscriptionFormComponent {
  private readonly platformId = inject(PLATFORM_ID);

  /** Ein bestehendes Abo. Ohne Ausgangswert legt das Formular eines an. */
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
