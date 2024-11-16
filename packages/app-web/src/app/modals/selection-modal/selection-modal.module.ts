import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SelectionModalComponent } from './selection-modal.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
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
  IonCheckbox,
  IonNote,
} from '@ionic/angular/standalone';
import { PlayerModule } from '../../components/player/player.module';

@NgModule({
  declarations: [SelectionModalComponent],
  exports: [SelectionModalComponent],
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
    IonCheckbox,
    IonNote,
    PlayerModule,
    ReactiveFormsModule,
  ],
})
export class SelectionModalModule {}
