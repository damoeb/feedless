import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConsoleButtonComponent } from './console-button.component';
import { IonicModule } from '@ionic/angular';
import { BubbleModule } from '../bubble/bubble.module';
import { RouterLink } from '@angular/router';
import { CodeEditorModalModule } from '../../modals/code-editor-modal/code-editor-modal.module';

@NgModule({
  declarations: [ConsoleButtonComponent],
  exports: [ConsoleButtonComponent],
  imports: [
    CommonModule,
    IonicModule,
    BubbleModule,
    RouterLink,
    CodeEditorModalModule,
  ],
})
export class ConsoleButtonModule {}
