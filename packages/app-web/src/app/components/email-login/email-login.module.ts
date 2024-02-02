import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmailLoginComponent } from './email-login.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [EmailLoginComponent],
  exports: [EmailLoginComponent],
  imports: [CommonModule, IonicModule, FormsModule, ReactiveFormsModule],
})
export class EmailLoginModule {}
