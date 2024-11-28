import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TrackerEditPage } from './tracker-edit.page';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TrackerEditRoutingModule } from './tracker-edit-routing.module';



import {
  IonAccordion,
  IonAccordionGroup,
  IonButton,
  IonCol,
  IonContent,
  IonGrid,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonRadio,
  IonRadioGroup,
  IonReorder,
  IonReorderGroup,
  IonRow,
  IonSelect,
  IonSelectOption,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    TrackerEditRoutingModule,
    IonContent,
    IonGrid,
    IonRow,
    IonCol,
    IonAccordionGroup,
    IonAccordion,
    IonItem,
    IonLabel,
    IonList,
    IonReorderGroup,
    IonSelect,
    IonSelectOption,
    IonButton,
    IonIcon,
    IonReorder,
    IonRadioGroup,
    IonRadio,
    TrackerEditPage,
],
})
export class TrackerEditPageModule {}
