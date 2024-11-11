import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImageDiffComponent } from './image-diff.component';
import 'img-comparison-slider';
import { TextDiffModule } from '../text-diff/text-diff.module';
import { IonIcon } from '@ionic/angular/standalone';

@NgModule({
  imports: [CommonModule, TextDiffModule, IonIcon],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [ImageDiffComponent],
  exports: [ImageDiffComponent],
})
export class ImageDiffModule {}
