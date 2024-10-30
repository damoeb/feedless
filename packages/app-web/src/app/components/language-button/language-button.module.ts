import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LanguageButtonComponent } from './language-button.component';
import { IonicModule } from '@ionic/angular';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [LanguageButtonComponent],
  exports: [LanguageButtonComponent],
  imports: [CommonModule, IonicModule, ReactiveFormsModule],
})
export class LanguageButtonModule {}
