import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';

import { UntoldNotesPageRoutingModule } from './untold-notes-product-routing.module';

import { UntoldNotesProductPage } from './untold-notes-product.page';
import { DarkModeButtonModule } from '../../components/dark-mode-button/dark-mode-button.module';
import { LoginButtonModule } from '../../components/login-button/login-button.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { OfflineModule } from '../../offline.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    UntoldNotesPageRoutingModule,
    DarkModeButtonModule,
    LoginButtonModule,
    ReactiveFormsModule,
    FormsModule,
    SearchbarModule,
    OfflineModule,
  ],
  // providers: [NotebookService],
  declarations: [UntoldNotesProductPage],
})
export class UntoldNotesProductModule {}
