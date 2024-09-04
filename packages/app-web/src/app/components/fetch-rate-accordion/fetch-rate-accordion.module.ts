import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FetchRateAccordionComponent } from './fetch-rate-accordion.component';
import { IonicModule } from '@ionic/angular';
import { ReactiveFormsModule } from '@angular/forms';
import { FilterItemsAccordionModule } from '../filter-items-accordion/filter-items-accordion.module';

@NgModule({
  declarations: [FetchRateAccordionComponent],
  exports: [FetchRateAccordionComponent],
  imports: [
    CommonModule,
    IonicModule,
    ReactiveFormsModule,
    FilterItemsAccordionModule,
  ],
})
export class FetchRateAccordionModule {}
