import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { PricingComponent } from './pricing.component';
import { ProductHeadlineModule } from '../../components/product-headline/product-headline.module';
import { PlanColumnModule } from '../../components/plan-column/plan-column.module';
import {
  IonSegment,
  IonSegmentButton,
  IonLabel,
  IonRow,
  IonCol,
  IonNote,
  IonButton,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ProductHeadlineModule,
    ReactiveFormsModule,
    PlanColumnModule,
    IonSegment,
    IonSegmentButton,
    IonLabel,
    IonRow,
    IonCol,
    IonNote,
    IonButton,
  ],
  declarations: [PricingComponent],
  exports: [PricingComponent],
})
export class PricingModule {}
