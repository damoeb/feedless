import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { ItemPageRoutingModule } from './item-routing.module';

import { ItemPage } from './item.page';
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
  declarations: [ItemPage],
})
export class ItemPageModule {}
