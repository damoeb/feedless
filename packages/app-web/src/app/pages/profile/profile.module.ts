import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ProfilePageRoutingModule } from './profile-routing.module';

import { ProfilePage } from './profile.page';


import {
  IonButton,
  IonCol,
  IonContent,
  IonIcon,
  IonInput,
  IonItem,
  IonLabel,
  IonList,
  IonNote,
  IonRow,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ProfilePageRoutingModule,
    ReactiveFormsModule,
    IonContent,
    IonRow,
    IonCol,
    IonList,
    IonLabel,
    IonInput,
    IonButton,
    IonItem,
    IonIcon,
    IonNote,
    ProfilePage,
],
})
export class ProfilePageModule {}
