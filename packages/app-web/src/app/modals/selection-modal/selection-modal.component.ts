import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import {
  IonButton,
  IonButtons,
  IonCheckbox,
  IonContent,
  IonHeader,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonNote,
  IonTitle,
  IonToolbar,
  ModalController,
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { closeOutline, trashOutline } from 'ionicons/icons';
import { relativeTimeOrElse } from '../../components/agents/agents.component';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';

export interface SelectionModalComponentProps<T> {
  selectables: SelectableEntity<T>[];
  title: string;
  description: string;
}

export type SelectableEntity<T> = {
  entity: T;
  disabled?: boolean;
  selected?: boolean;
  label: string;
  note?: string;
};
type EntityWithFormControl<T> = {
  entity: SelectableEntity<T>;
  formControl: FormControl<boolean>;
};

@Component({
  selector: 'app-selection-modal',
  templateUrl: './selection-modal.component.html',
  styleUrls: ['./selection-modal.component.scss'],
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonHeader,
    IonToolbar,
    IonButtons,
    IonButton,
    IonIcon,
    IonTitle,
    IonContent,
    IonList,
    IonItem,
    IonCheckbox,
    FormsModule,
    ReactiveFormsModule,
    IonLabel,
    IonNote,
  ],
})
export class SelectionModalComponent<T>
  implements SelectionModalComponentProps<T>, OnInit, OnDestroy
{
  private readonly modalCtrl = inject(ModalController);
  private readonly changeRef = inject(ChangeDetectorRef);

  selectables: SelectableEntity<T>[] = [];
  description: string;
  title: string;
  selectAllFormControl: FormControl<boolean> = new FormControl(false);
  entitiesWithFormControl: EntityWithFormControl<T>[] = [];
  private subscriptions: Subscription[] = [];

  constructor() {
    addIcons({ closeOutline, trashOutline });
  }

  closeModal() {
    return this.modalCtrl.dismiss([]);
  }

  protected readonly fromNow = relativeTimeOrElse;

  ngOnInit(): void {
    this.subscriptions.push(
      this.selectAllFormControl.valueChanges.subscribe((selectAll) => {
        this.entitiesWithFormControl.forEach((entityWithFormControl) => {
          debugger;
          entityWithFormControl.formControl.setValue(selectAll);
          this.changeRef.markForCheck();
        });
      }),
    );
    this.entitiesWithFormControl = this.selectables.map((selectable) => {
      const formControl = new FormControl<boolean>(
        selectable.selected ?? false,
      );
      if (selectable.disabled) {
        formControl.disable();
      }

      return {
        entity: selectable,
        formControl,
      };
    });
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  importSelected() {
    return this.modalCtrl.dismiss(
      this.entitiesWithFormControl
        .filter((ewf) => ewf.formControl.value)
        .map((ewf) => ewf.entity.entity),
    );
  }

  createTrackById(entityWithFormControl: EntityWithFormControl<T>) {
    return `${entityWithFormControl.formControl.value}-${entityWithFormControl.entity.label}`;
  }
}
