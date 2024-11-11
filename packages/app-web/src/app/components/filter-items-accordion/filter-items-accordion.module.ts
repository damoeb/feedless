import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FilterItemsAccordionComponent } from './filter-items-accordion.component';
import { ReactiveFormsModule } from '@angular/forms';
import { CodeEditorModule } from '../../elements/code-editor/code-editor.module';
import {
  IonAccordion,
  IonItem,
  IonLabel,
  IonChip,
  IonNote,
  IonCheckbox,
  IonText,
  IonSelect,
  IonSelectOption,
  IonInput,
  IonButton,
  IonIcon,
  IonTextarea,
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [FilterItemsAccordionComponent],
  exports: [FilterItemsAccordionComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    CodeEditorModule,
    IonAccordion,
    IonItem,
    IonLabel,
    IonChip,
    IonNote,
    IonCheckbox,
    IonText,
    IonSelect,
    IonSelectOption,
    IonInput,
    IonButton,
    IonIcon,
    IonTextarea,
  ],
})
export class FilterItemsAccordionModule {}
