import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { WritePageRoutingModule } from './write-routing.module';

import { WritePage } from './write.page';
import { BaseModule } from '../../components/base/base.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    WritePageRoutingModule,
    BaseModule,
  ],
  declarations: [WritePage],
})
export class WritePageModule {}
