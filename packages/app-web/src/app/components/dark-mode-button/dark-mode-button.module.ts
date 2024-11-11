import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DarkModeButtonComponent } from './dark-mode-button.component';
import { IonButton, IonIcon } from '@ionic/angular/standalone';

@NgModule({
  declarations: [DarkModeButtonComponent],
  exports: [DarkModeButtonComponent],
  imports: [CommonModule, IonButton, IonIcon],
})
export class DarkModeButtonModule {}
