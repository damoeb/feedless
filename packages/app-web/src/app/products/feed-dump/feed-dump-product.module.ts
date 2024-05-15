import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';

import { FeedDumpProductRoutingModule } from './feed-dump-product-routing.module';

import { FeedDumpProductPage } from './feed-dump-product.page';
import { DarkModeButtonModule } from '../../components/dark-mode-button/dark-mode-button.module';
import { LoginButtonModule } from '../../components/login-button/login-button.module';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { TrialWarningModule } from '../../components/trial-warning/trial-warning.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    DarkModeButtonModule,
    FeedDumpProductRoutingModule,
    LoginButtonModule,
    SearchbarModule,
    TrialWarningModule,
  ],
  declarations: [FeedDumpProductPage],
})
export class FeedDumpProductModule {}
