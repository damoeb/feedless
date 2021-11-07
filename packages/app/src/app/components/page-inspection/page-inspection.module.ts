import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PageInspectionComponent } from './page-inspection.component';
import { IonicModule } from '@ionic/angular';
import { BubbleModule } from '../bubble/bubble.module';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [PageInspectionComponent],
  exports: [PageInspectionComponent],
  imports: [CommonModule, IonicModule, BubbleModule, FormsModule],
})
export class PageInspectionModule {}
