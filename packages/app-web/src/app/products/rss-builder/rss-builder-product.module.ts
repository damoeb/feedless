import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RssBuilderPageRoutingModule } from './rss-builder-product-routing.module';

import { RssBuilderProductPage } from './rss-builder-product.page';
import { DarkModeButtonModule } from '../../components/dark-mode-button/dark-mode-button.module';
import { LoginButtonModule } from '../../components/login-button/login-button.module';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { TrialWarningModule } from '../../components/trial-warning/trial-warning.module';
import { BubbleModule } from '../../components/bubble/bubble.module';
import { AgentsButtonModule } from '../../components/agents-button/agents-button.module';
import { RepositoriesButtonModule } from '../../components/repositories-button/repositories-button.module';
import { NotificationsButtonModule } from '../../components/notifications-button/notifications-button.module';
import {
  IonHeader,
  IonToolbar,
  IonButtons,
  IonMenuButton,
  IonButton,
  IonIcon,
  IonContent,
  IonRouterOutlet,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    RssBuilderPageRoutingModule,
    DarkModeButtonModule,
    LoginButtonModule,
    SearchbarModule,
    TrialWarningModule,
    BubbleModule,
    AgentsButtonModule,
    RepositoriesButtonModule,
    NotificationsButtonModule,
    IonHeader,
    IonToolbar,
    IonButtons,
    IonMenuButton,
    IonButton,
    IonIcon,
    IonContent,
    IonRouterOutlet,
  ],
  declarations: [RssBuilderProductPage],
})
export class RssBuilderProductModule {}
