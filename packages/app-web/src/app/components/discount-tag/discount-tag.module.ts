import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';

import { DiscountTagComponent } from './discount-tag.component';

@NgModule({
  imports: [CommonModule, IonicModule],
  declarations: [DiscountTagComponent],
  exports: [DiscountTagComponent],
})
export class DiscountTagModule {}
