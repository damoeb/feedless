import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BucketCreateComponent } from './bucket-create.component';
import { IonicModule } from '@ionic/angular';
import { BubbleModule } from '../bubble/bubble.module';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [BucketCreateComponent],
  exports: [BucketCreateComponent],
  imports: [CommonModule, IonicModule, BubbleModule, FormsModule],
})
export class BucketCreateModule {}
