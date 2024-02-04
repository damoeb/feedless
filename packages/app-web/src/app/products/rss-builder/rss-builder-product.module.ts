import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';

import { RssBuilderPageRoutingModule } from './rss-builder-product-routing.module';

import { RssBuilderProductPage } from './rss-builder-product.page';
import { DarkModeButtonModule } from '../../components/dark-mode-button/dark-mode-button.module';
import { LoginButtonModule } from '../../components/login-button/login-button.module';
import { RssBuilderMenuModule } from './rss-builder-menu/rss-builder-menu.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    RssBuilderPageRoutingModule,
    RssBuilderMenuModule,
    DarkModeButtonModule,
    LoginButtonModule,
  ],
  declarations: [RssBuilderProductPage],
})
export class RssBuilderProductModule {}
