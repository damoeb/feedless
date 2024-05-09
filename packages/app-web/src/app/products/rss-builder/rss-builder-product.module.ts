import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';

import { RssBuilderPageRoutingModule } from './rss-builder-product-routing.module';

import { RssBuilderProductPage } from './rss-builder-product.page';
import { DarkModeButtonModule } from '../../components/dark-mode-button/dark-mode-button.module';
import { LoginButtonModule } from '../../components/login-button/login-button.module';
import { RssBuilderMenuModule } from './rss-builder-menu/rss-builder-menu.module';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { TrialWarningModule } from '../../components/trial-warning/trial-warning.module';
import { BubbleModule } from '../../components/bubble/bubble.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    RssBuilderPageRoutingModule,
    RssBuilderMenuModule,
    DarkModeButtonModule,
    LoginButtonModule,
    SearchbarModule,
    TrialWarningModule,
    BubbleModule,
  ],
  declarations: [RssBuilderProductPage],
})
export class RssBuilderProductModule {}
