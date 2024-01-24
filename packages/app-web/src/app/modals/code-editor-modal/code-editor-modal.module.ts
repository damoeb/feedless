import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CodeEditorModalComponent } from './code-editor-modal.component';
import { IonicModule } from '@ionic/angular';
import { CodeEditorModule } from '../../elements/code-editor/code-editor.module';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [CodeEditorModalComponent],
  exports: [CodeEditorModalComponent],
  imports: [CommonModule, IonicModule, CodeEditorModule, ReactiveFormsModule]
})
export class CodeEditorModalModule {
}
