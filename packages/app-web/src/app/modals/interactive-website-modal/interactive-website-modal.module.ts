import { NgModule } from '@angular/core';
import { JsonPipe, NgClass } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { InteractiveWebsiteModalComponent } from './interactive-website-modal.component';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonFooter,
  IonHeader,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonRow,
  IonSelect,
  IonSelectOption,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';
import { InteractiveWebsiteComponent } from '../../components/interactive-website/interactive-website.component';

@NgModule({
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IonIcon,
    IonContent,
    IonRow,
    InteractiveWebsiteComponent,
    IonList,
    NgClass,
    IonItem,
    IonSelect,
    FormsModule,
    ReactiveFormsModule,
    IonSelectOption,
    IonLabel,
    IonFooter,
    JsonPipe,
  ],
  declarations: [InteractiveWebsiteModalComponent],
  exports: [InteractiveWebsiteModalComponent],
})
export class InteractiveWebsiteModalModule {}
