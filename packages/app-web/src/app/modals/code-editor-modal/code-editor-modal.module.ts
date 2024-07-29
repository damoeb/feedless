import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CodeEditorModalComponent } from './code-editor-modal.component';
import { CodeEditorModule } from '../../elements/code-editor/code-editor.module';

@NgModule({
  declarations: [CodeEditorModalComponent],
  exports: [CodeEditorModalComponent],
  imports: [
    CommonModule,
    IonicModule,
    RouterLink,
    FormsModule,
    ReactiveFormsModule,
    CodeEditorModule,
  ],
})
export class CodeEditorModalModule {}
