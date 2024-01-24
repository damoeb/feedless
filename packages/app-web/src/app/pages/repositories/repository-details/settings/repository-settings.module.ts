import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { RepositorySettingsPageRoutingModule } from './repository-delivery-routing.module';

import { RepositorySettingsPage } from './repository-settings-page.component';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    RepositorySettingsPageRoutingModule,
  ],
  declarations: [RepositorySettingsPage]
})
export class RepositorySettingsPageModule {
}
