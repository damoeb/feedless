import { Component, inject, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { closeOutline, trashOutline } from 'ionicons/icons';
import { relativeTimeOrElse } from '../../components/agents/agents.component';
import { FormControl } from '@angular/forms';

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
  standalone: false,
})
export class SelectionModalComponent<T>
  implements SelectionModalComponentProps<T>, OnInit
{
  private readonly modalCtrl = inject(ModalController);

  selectables: SelectableEntity<T>[] = [];
  description: string;
  title: string;
  entitiesWithFormControl: EntityWithFormControl<T>[] = [];

  constructor() {
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
