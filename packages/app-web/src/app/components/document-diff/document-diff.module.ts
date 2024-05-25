import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { DocumentDiffComponent } from './document-diff.component';
import 'img-comparison-slider';

@NgModule({
  imports: [CommonModule, IonicModule],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [DocumentDiffComponent],
  exports: [
    DocumentDiffComponent
  ]
})
export class DocumentDiffModule {}
