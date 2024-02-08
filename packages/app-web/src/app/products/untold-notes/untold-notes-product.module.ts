import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';

import { UntoldNotesPageRoutingModule } from './untold-notes-product-routing.module';

import { UntoldNotesProductPage } from './untold-notes-product.page';
import { DarkModeButtonModule } from '../../components/dark-mode-button/dark-mode-button.module';
import { LoginButtonModule } from '../../components/login-button/login-button.module';
import { UntoldNotesMenuModule } from './untold-notes-menu/untold-notes-menu.module';
import { NotebookService } from './services/notebook.service';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    UntoldNotesPageRoutingModule,
    UntoldNotesMenuModule,
    DarkModeButtonModule,
    LoginButtonModule,
    ReactiveFormsModule,
    FormsModule,
    SearchbarModule,
  ],
  providers: [NotebookService],
  declarations: [UntoldNotesProductPage],
})
export class UntoldNotesProductModule {}
