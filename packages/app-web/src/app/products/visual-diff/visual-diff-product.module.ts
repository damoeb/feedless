import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VisualDiffProductRoutingModule } from './visual-diff-product-routing.module';

import { VisualDiffProductPage } from './visual-diff-product.page';
import { DarkModeButtonModule } from '../../components/dark-mode-button/dark-mode-button.module';
import { LoginButtonModule } from '../../components/login-button/login-button.module';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { TrialWarningModule } from '../../components/trial-warning/trial-warning.module';
import { AgentsButtonModule } from '../../components/agents-button/agents-button.module';
import { RepositoriesButtonModule } from '../../components/repositories-button/repositories-button.module';
import {
  IonHeader,
  IonToolbar,
  IonButtons,
  IonMenuButton,
  IonButton,
  IonContent,
  IonRouterOutlet,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    VisualDiffProductRoutingModule,
    DarkModeButtonModule,
    LoginButtonModule,
    SearchbarModule,
    TrialWarningModule,
    AgentsButtonModule,
    RepositoriesButtonModule,
    IonHeader,
    IonToolbar,
    IonButtons,
    IonMenuButton,
    IonButton,
    IonContent,
    IonRouterOutlet,
  ],
  declarations: [VisualDiffProductPage],
})
export class VisualDiffProductModule {}
