import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ProfilePageRoutingModule } from './profile-routing.module';

import { ProfilePage } from './profile.page';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';
import { RemoveIfProdModule } from '../../directives/remove-if-prod/remove-if-prod.module';
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
    FeedlessHeaderModule,
    RemoveIfProdModule,
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
  ],
  declarations: [ProfilePage],
})
export class ProfilePageModule {}
