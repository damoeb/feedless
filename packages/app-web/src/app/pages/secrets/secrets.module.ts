import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { SecretsPageRoutingModule } from './secrets-routing.module';

import { SecretsPage } from './secrets.page';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    SecretsPageRoutingModule,
    ReactiveFormsModule,
    FeedlessHeaderModule,
  ],
  declarations: [SecretsPage],
})
export class SecretsPageModule {}
