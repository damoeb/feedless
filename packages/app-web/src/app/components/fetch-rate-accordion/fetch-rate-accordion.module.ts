import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FetchRateAccordionComponent } from './fetch-rate-accordion.component';
import { ReactiveFormsModule } from '@angular/forms';
import { FilterItemsAccordionModule } from '../filter-items-accordion/filter-items-accordion.module';
import {
  IonRow,
  IonCol,
  IonSelect,
  IonSelectOption,
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [FetchRateAccordionComponent],
  exports: [FetchRateAccordionComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FilterItemsAccordionModule,
    IonRow,
    IonCol,
    IonSelect,
    IonSelectOption,
  ],
})
export class FetchRateAccordionModule {}
