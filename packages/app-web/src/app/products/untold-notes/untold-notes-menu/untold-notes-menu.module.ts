import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UntoldNotesMenuComponent } from './untold-notes-menu.component';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';
import { DarkModeButtonModule } from '../../../components/dark-mode-button/dark-mode-button.module';
import { LoginButtonModule } from '../../../components/login-button/login-button.module';

@NgModule({
  declarations: [UntoldNotesMenuComponent],
  exports: [UntoldNotesMenuComponent],
  imports: [CommonModule, IonicModule, RouterLink, DarkModeButtonModule, LoginButtonModule]
})
export class UntoldNotesMenuModule {}
