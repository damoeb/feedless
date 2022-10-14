import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { BucketCreatePageRoutingModule } from './bucket-create-routing.module';

import { BucketCreatePage } from './bucket-create.page';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    BucketCreatePageRoutingModule,
    ReactiveFormsModule
  ],
  declarations: [BucketCreatePage]
})
export class BucketCreatePageModule {}
