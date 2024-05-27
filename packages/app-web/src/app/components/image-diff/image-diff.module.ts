import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { ImageDiffComponent } from './image-diff.component';
import 'img-comparison-slider';
import { TextDiffModule } from '../text-diff/text-diff.module';

@NgModule({
  imports: [CommonModule, IonicModule, TextDiffModule],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [ImageDiffComponent],
  exports: [
    ImageDiffComponent
  ]
})
export class ImageDiffModule {}
