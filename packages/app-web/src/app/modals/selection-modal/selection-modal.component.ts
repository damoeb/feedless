import { Component, OnInit } from '@angular/core';
import {
  ModalController,
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
  IonLabel,
  IonNote,
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { closeOutline, trashOutline } from 'ionicons/icons';
import { relativeTimeOrElse } from '../../components/agents/agents.component';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';

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
  selector: 'app-tags-modal',
  templateUrl: './selection-modal.component.html',
  styleUrls: ['./selection-modal.component.scss'],
  imports: [
    IonHeader,
    IonToolbar,
    IonButtons,
    IonButton,
    IonIcon,
    IonTitle,
    IonContent,
    IonList,
    NgFor,
    IonItem,
    IonCheckbox,
    FormsModule,
    ReactiveFormsModule,
    IonLabel,
    NgIf,
    IonNote,
  ],
  standalone: true,
})
export class SelectionModalComponent<T>
  implements SelectionModalComponentProps<T>, OnInit
{
  selectables: SelectableEntity<T>[] = [];
  description: string;
  title: string;
  entitiesWithFormControl: EntityWithFormControl<T>[] = [];

  constructor(private readonly modalCtrl: ModalController) {
    addIcons({ closeOutline, trashOutline });
  }

  closeModal() {
    return this.modalCtrl.dismiss([]);
  }

  protected readonly fromNow = relativeTimeOrElse;

  ngOnInit(): void {
    this.entitiesWithFormControl = this.selectables.map((selectable) => {
      const formControl = new FormControl<boolean>(selectable.selected);
      if (selectable.disabled) {
        formControl.disable();
      }

      return {
        entity: selectable,
        formControl,
      };
    });
  }

  importSelected() {
    return this.modalCtrl.dismiss(
      this.entitiesWithFormControl
        .filter((ewf) => ewf.formControl.value)
        .map((ewf) => ewf.entity.entity),
    );
  }
}
