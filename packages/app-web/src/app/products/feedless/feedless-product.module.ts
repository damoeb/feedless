import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedlessProductRoutingModule } from './feedless-product-routing.module';

import { FeedlessProductPage } from './feedless-product.page';
import { ProductTitleModule } from '../../components/product-title/product-title.module';
import { DarkModeButtonModule } from '../../components/dark-mode-button/dark-mode-button.module';
import { LoginButtonModule } from '../../components/login-button/login-button.module';
import { FeedlessMenuModule } from './feedless-menu/feedless-menu.module';
import { AgentsButtonModule } from '../../components/agents-button/agents-button.module';
import { RepositoriesButtonModule } from '../../components/repositories-button/repositories-button.module';
import { IonContent, IonRouterOutlet } from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FeedlessProductRoutingModule,
    ProductTitleModule,
    FeedlessMenuModule,
    DarkModeButtonModule,
    LoginButtonModule,
    AgentsButtonModule,
    RepositoriesButtonModule,
    IonContent,
    IonRouterOutlet,
  ],
  declarations: [FeedlessProductPage],
})
export class FeedlessProductModule {}
