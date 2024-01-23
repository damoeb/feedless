import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MagicLinkLoginComponent } from './magic-link-login.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [MagicLinkLoginComponent],
  exports: [MagicLinkLoginComponent],
  imports: [CommonModule, IonicModule, FormsModule, ReactiveFormsModule]
})
export class MagicLinkLoginModule {}
