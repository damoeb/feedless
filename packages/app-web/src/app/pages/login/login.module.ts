import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { LoginPageRoutingModule } from './login-routing.module';

import { LoginPage } from './login.page';
import { PageHeaderModule } from '../../components/page-header/page-header.module';
import { MailCodeLoginModule } from '../../components/magic-link-login/mail-code-login.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    LoginPageRoutingModule,
    PageHeaderModule,
    MailCodeLoginModule,
  ],
  declarations: [LoginPage],
})
export class LoginPageModule {}
