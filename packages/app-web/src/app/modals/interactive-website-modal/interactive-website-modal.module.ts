import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { InteractiveWebsiteModalComponent } from './interactive-website-modal.component';
import { EmbeddedImageModule } from '../../components/embedded-image/embedded-image.module';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { InteractiveWebsiteModule } from '../../components/interactive-website/interactive-website.module';
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

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    EmbeddedImageModule,
    SearchbarModule,
    InteractiveWebsiteModule,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IonIcon,
    IonContent,
    IonRow,
    IonList,
    IonItem,
    IonSelect,
    IonSelectOption,
    IonLabel,
    IonFooter,
  ],
  declarations: [InteractiveWebsiteModalComponent],
  exports: [InteractiveWebsiteModalComponent],
})
export class InteractiveWebsiteModalModule {}
