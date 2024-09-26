import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { ProfilePageRoutingModule } from './profile-routing.module';

import { ProfilePage } from './profile.page';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';
import { RemoveIfProdModule } from '../../directives/remove-if-prod/remove-if-prod.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    ProfilePageRoutingModule,
    ReactiveFormsModule,
    FeedlessHeaderModule,
    RemoveIfProdModule,
  ],
  declarations: [ProfilePage],
})
export class ProfilePageModule {}
