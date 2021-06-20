import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { WritePageRoutingModule } from './write-routing.module';

import { WritePage } from './write.page';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    WritePageRoutingModule
  ],
  declarations: [WritePage]
})
export class WritePageModule {}
