import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DarkModeButtonComponent } from './dark-mode-button.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [DarkModeButtonComponent],
  exports: [DarkModeButtonComponent],
  imports: [CommonModule, IonicModule]
})
export class DarkModeButtonModule {
}
