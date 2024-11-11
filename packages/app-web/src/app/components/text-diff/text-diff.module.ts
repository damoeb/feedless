import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TextDiffComponent } from './text-diff.component';
import 'img-comparison-slider';
import { CodeEditorModule } from '../../elements/code-editor/code-editor.module';

@NgModule({
  imports: [CommonModule, CodeEditorModule],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [TextDiffComponent],
  exports: [TextDiffComponent],
})
export class TextDiffModule {}
