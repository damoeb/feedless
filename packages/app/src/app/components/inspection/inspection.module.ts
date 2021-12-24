import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InspectionComponent } from './inspection.component';
import { IonicModule } from '@ionic/angular';
import { BubbleModule } from '../bubble/bubble.module';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

@NgModule({
  declarations: [InspectionComponent],
  exports: [InspectionComponent],
  imports: [
    CommonModule,
    IonicModule,
    BubbleModule,
    FormsModule,
    HttpClientModule,
  ],
})
export class InspectionModule {}
