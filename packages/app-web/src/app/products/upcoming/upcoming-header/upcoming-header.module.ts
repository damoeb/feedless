import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UpcomingHeaderComponent } from './upcoming-header.component';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DarkModeButtonModule } from '../../../components/dark-mode-button/dark-mode-button.module';
import { MapModule } from '../../../components/map/map.module';
import {
  IonHeader,
  IonToolbar,
  IonButton,
  IonIcon,
  IonSearchbar,
  IonSelect,
  IonSelectOption,
  IonButtons,
  IonList,
  IonItem,
  IonTitle,
  IonNote,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    DarkModeButtonModule,
    MapModule,
    IonHeader,
    IonToolbar,
    IonButton,
    IonIcon,
    IonSearchbar,
    IonSelect,
    IonSelectOption,
    IonButtons,
    IonList,
    IonItem,
    IonTitle,
    IonNote,
  ],
  declarations: [UpcomingHeaderComponent],
  exports: [UpcomingHeaderComponent],
})
export class UpcomingHeaderModule {}
