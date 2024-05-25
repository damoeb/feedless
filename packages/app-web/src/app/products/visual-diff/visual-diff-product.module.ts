import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';

import { VisualDiffProductRoutingModule } from './visual-diff-product-routing.module';

import { VisualDiffProductPage } from './visual-diff-product.page';
import { DarkModeButtonModule } from '../../components/dark-mode-button/dark-mode-button.module';
import { LoginButtonModule } from '../../components/login-button/login-button.module';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { VisualDiffMenuModule } from './visual-diff-menu/visual-diff-menu.module';
import { TrialWarningModule } from '../../components/trial-warning/trial-warning.module';
import { AgentsButtonModule } from '../../components/agents-button/agents-button.module';
import { RepositoriesButtonModule } from '../../components/repositories-button/repositories-button.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    VisualDiffMenuModule,
    VisualDiffProductRoutingModule,
    DarkModeButtonModule,
    LoginButtonModule,
    SearchbarModule,
    TrialWarningModule,
    AgentsButtonModule,
    RepositoriesButtonModule,
  ],
  declarations: [VisualDiffProductPage],
})
export class VisualDiffProductModule {}
