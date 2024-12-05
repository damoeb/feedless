import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ImportOpmlModalComponent } from './import-opml-modal.component';
import {
  IonButton,
  IonButtons,
  IonCheckbox,
  IonContent,
  IonFooter,
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

@NgModule({
  declarations: [ImportOpmlModalComponent],
  exports: [ImportOpmlModalComponent],
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IonIcon,
    IonContent,
    IonList,
    IonItem,
    IonCheckbox,
    FormsModule,
    ReactiveFormsModule,
    IonLabel,
    IonFooter,
    IonNote,
  ],
  providers: [ModalController],
})
export class ImportOpmlModalModule {}
