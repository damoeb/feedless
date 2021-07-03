import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { ItemPageRoutingModule } from './item-routing.module';

import { ItemPage } from './item.page';
import { SearchFieldModule } from '../../components/search-field/search-field.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    ItemPageRoutingModule,
    SearchFieldModule,
  ],
  declarations: [ItemPage],
})
export class ItemPageModule {}
