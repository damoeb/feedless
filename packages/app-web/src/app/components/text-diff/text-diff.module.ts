import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { TextDiffComponent } from './text-diff.component';
import 'img-comparison-slider';

@NgModule({
  imports: [CommonModule, IonicModule],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [TextDiffComponent],
  exports: [
    TextDiffComponent
  ]
})
export class TextDiffModule {}
