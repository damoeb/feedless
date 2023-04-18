import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MagicLinkLoginComponent } from './magic-link-login.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [MagicLinkLoginComponent],
  exports: [MagicLinkLoginComponent],
  imports: [CommonModule, IonicModule, FormsModule],
})
export class MagicLinkLoginModule {}
