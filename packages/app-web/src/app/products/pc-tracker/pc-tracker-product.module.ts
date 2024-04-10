import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';

import { PageChangeTrackerPageRoutingModule } from './pc-tracker-product-routing.module';

import { PcTrackerProductPage } from './pc-tracker-product.page';
import { DarkModeButtonModule } from '../../components/dark-mode-button/dark-mode-button.module';
import { LoginButtonModule } from '../../components/login-button/login-button.module';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { TrialWarningModule } from '../../components/trial-warning/trial-warning.module';
import { TrackerEditModalModule } from './tracker-edit/tracker-edit-modal.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    DarkModeButtonModule,
    PageChangeTrackerPageRoutingModule,
    LoginButtonModule,
    SearchbarModule,
    TrackerEditModalModule,
    TrialWarningModule
  ],
  declarations: [PcTrackerProductPage],
})
export class PcTrackerProductModule {}
