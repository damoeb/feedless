import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { LoginPageRoutingModule } from './login-routing.module';

import { LoginPage } from './login.page';
import { PageHeaderModule } from '../../components/page-header/page-header.module';
import { MagicLinkLoginModule } from '../../components/magic-link-login/magic-link-login.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    LoginPageRoutingModule,
    PageHeaderModule,
    MagicLinkLoginModule,
  ],
  declarations: [LoginPage],
})
export class LoginPageModule {}
