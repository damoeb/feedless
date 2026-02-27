import { Component, computed, inject, signal } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonInput,
  IonItem,
  IonLabel,
  IonList,
  IonRadio,
  IonRadioGroup,
  IonTextarea,
  IonTitle,
  IonToolbar,
  ModalController,
} from '@ionic/angular/standalone';
// eslint-disable-next-line @nx/enforce-module-boundaries
import { IconComponent } from '@feedless/components';

export type EventType = 'single' | 'series';

@Component({
  selector: 'app-add-event-modal',
  templateUrl: './add-event-modal.component.html',
  styleUrls: ['./add-event-modal.component.scss'],
  standalone: true,
  imports: [
    ReactiveFormsModule,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IonContent,
    IonList,
    IonItem,
    IonLabel,
    IonInput,
    IonTextarea,
    IonRadioGroup,
    IonRadio,
    IconComponent,
  ],
})
export class AddEventModalComponent {
  private readonly modalCtrl = inject(ModalController);

  readonly totalSteps = 4;
  readonly currentStep = signal(1);

  readonly eventForm = new FormGroup({
    eventType: new FormControl<EventType>('single', { nonNullable: true }),
    title: new FormControl('', { validators: [Validators.required] }),
    description: new FormControl('', { validators: [Validators.required] }),
    place: new FormControl(''),
    date: new FormControl('', { validators: [Validators.required] }),
    startTime: new FormControl('', { validators: [Validators.required] }),
    duration: new FormControl('60', { validators: [Validators.required] }),
    isCommercial: new FormControl(false, { nonNullable: true }),
    price: new FormControl<number | null>(null),
    promotionText: new FormControl(''),
    promotionChannels: new FormControl<string[]>([]),
  });

  readonly stepTitles = [
    'Event-Typ',
    'Details',
    'Zielgruppe & Preis',
    'Promotion',
  ] as const;

  readonly canGoNext = computed(() => {
    const step = this.currentStep();
    if (step === 1) return true;
    if (step === 2) {
      const g = this.eventForm;
      return (
        g.controls.title.valid &&
        g.controls.description.valid &&
        g.controls.date.valid &&
        g.controls.startTime.valid &&
        g.controls.duration.valid
      );
    }
    if (step === 3) return true;
    if (step === 4) return true;
    return false;
  });

  readonly isFirstStep = computed(() => this.currentStep() === 1);
  readonly isLastStep = computed(() => this.currentStep() === this.totalSteps);

  close() {
    return this.modalCtrl.dismiss();
  }

  next() {
    if (this.currentStep() < this.totalSteps) {
      this.currentStep.update((s) => s + 1);
    } else {
      this.submit();
    }
  }

  prev() {
    if (this.currentStep() > 1) {
      this.currentStep.update((s) => s - 1);
    }
  }

  goToStep(step: number) {
    if (step >= 1 && step <= this.totalSteps && step <= this.currentStep()) {
      this.currentStep.set(step);
    }
  }

  submit() {
    if (this.eventForm.valid) {
      this.modalCtrl.dismiss(this.eventForm.getRawValue(), 'submit');
    }
  }
}
