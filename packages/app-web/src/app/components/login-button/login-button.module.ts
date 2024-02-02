import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginButtonComponent } from './login-button.component';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';

@NgModule({
  declarations: [LoginButtonComponent],
  exports: [LoginButtonComponent],
  imports: [CommonModule, IonicModule, RouterLink],
})
export class LoginButtonModule {}
