import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { ProfilePageRoutingModule } from './profile-routing.module';

import { ProfilePage } from './profile.page';
import { PageHeaderModule } from '../../components/page-header/page-header.module';
import { ImportModalModule } from '../../modals/import-modal/import-modal.module';
import { FeatureStateModule } from '../../components/feature-state/feature-state.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    ProfilePageRoutingModule,
    PageHeaderModule,
    ReactiveFormsModule,
    ImportModalModule,
    FeatureStateModule,
  ],
  declarations: [ProfilePage],
})
export class ProfilePageModule {}
