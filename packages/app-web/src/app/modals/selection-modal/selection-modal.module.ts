import { NgModule } from '@angular/core';
import { SelectionModalComponent } from './selection-modal.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
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
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [SelectionModalComponent],
  exports: [SelectionModalComponent],
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
export class SelectionModalModule {}
