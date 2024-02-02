import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';

import { VisualDiffProductRoutingModule } from './visual-diff-product-routing.module';

import { VisualDiffProductPage } from './visual-diff-product.page';
import { DarkModeButtonModule } from '../../components/dark-mode-button/dark-mode-button.module';
import { LoginButtonModule } from '../../components/login-button/login-button.module';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    VisualDiffProductRoutingModule,
    DarkModeButtonModule,
    LoginButtonModule,
    SearchbarModule,
  ],
  declarations: [VisualDiffProductPage],
})
export class VisualDiffProductModule {}
