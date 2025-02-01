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
  IonInput,
  IonItem,
  IonLabel,
  IonList,
  IonReorder,
  IonReorderGroup,
  IonRow,
  IonSelect,
  IonSelectOption,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';
import { InteractiveWebsiteComponent } from '../../components/interactive-website/interactive-website.component';
import { InputComponent } from '../../elements/input/input.component';

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
    IonReorderGroup,
    IonReorder,
    IonInput,
    InputComponent,
  ],
  declarations: [InteractiveWebsiteModalComponent],
  exports: [InteractiveWebsiteModalComponent],
})
export class InteractiveWebsiteModalModule {}
