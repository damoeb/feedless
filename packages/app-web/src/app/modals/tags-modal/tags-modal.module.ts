import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TagsModalComponent } from './tags-modal.component';
import { FormsModule } from '@angular/forms';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import {
  IonHeader,
  IonToolbar,
  IonTitle,
  IonButtons,
  IonButton,
  IonIcon,
  IonContent,
  IonList,
  IonItem,
  IonLabel,
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [TagsModalComponent],
  exports: [TagsModalComponent],
  imports: [
    CommonModule,
    FormsModule,
    SearchbarModule,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IonIcon,
    IonContent,
    IonList,
    IonItem,
    IonLabel,
  ],
})
export class TagsModalModule {}
