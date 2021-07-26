import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { ItemPageRoutingModule } from './read-routing.module';

import { ReadPage } from './read.page';
import { ToolbarModule } from '../../components/toolbar/toolbar.module';
import { ReaderModule } from '../../components/reader/reader.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    ItemPageRoutingModule,
    ToolbarModule,
    ReaderModule,
  ],
  declarations: [ReadPage],
})
export class ItemPageModule {}
