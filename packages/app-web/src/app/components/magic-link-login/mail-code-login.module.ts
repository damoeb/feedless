import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MailCodeLoginComponent } from './mail-code-login.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [MailCodeLoginComponent],
  exports: [MailCodeLoginComponent],
  imports: [CommonModule, IonicModule, FormsModule, ReactiveFormsModule]
})
export class MailCodeLoginModule {}
