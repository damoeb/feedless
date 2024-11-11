import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PageChangeTrackerPageRoutingModule } from './change-tracker-product-routing.module';

import { ChangeTrackerProductPage } from './change-tracker-product.page';
import { DarkModeButtonModule } from '../../components/dark-mode-button/dark-mode-button.module';
import { LoginButtonModule } from '../../components/login-button/login-button.module';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { TrialWarningModule } from '../../components/trial-warning/trial-warning.module';
import { TrackerEditModalModule } from './tracker-edit/tracker-edit-modal.module';
import {
  IonHeader,
  IonToolbar,
  IonButtons,
  IonMenuButton,
  IonButton,
  IonIcon,
  IonContent,
  IonRouterOutlet,
  IonFooter,
  IonChip,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    DarkModeButtonModule,
    PageChangeTrackerPageRoutingModule,
    LoginButtonModule,
    SearchbarModule,
    TrackerEditModalModule,
    TrialWarningModule,
    IonHeader,
    IonToolbar,
    IonButtons,
    IonMenuButton,
    IonButton,
    IonIcon,
    IonContent,
    IonRouterOutlet,
    IonFooter,
    IonChip,
  ],
  declarations: [ChangeTrackerProductPage],
})
export class ChangeTrackerProductModule {}
