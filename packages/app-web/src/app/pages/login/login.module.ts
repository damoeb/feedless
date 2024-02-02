import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { LoginPageRoutingModule } from './login-routing.module';

import { LoginPage } from './login.page';
import { EmailLoginModule } from '../../components/email-login/email-login.module';
import { NewsletterModule } from '../../components/newsletter/newsletter.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    LoginPageRoutingModule,
    EmailLoginModule,
    NewsletterModule,
  ],
  declarations: [LoginPage],
})
export class EmailLoginPageModule {}
