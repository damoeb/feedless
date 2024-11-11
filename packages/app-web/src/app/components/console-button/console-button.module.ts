import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConsoleButtonComponent } from './console-button.component';
import { BubbleModule } from '../bubble/bubble.module';
import { RouterLink } from '@angular/router';
import { CodeEditorModalModule } from '../../modals/code-editor-modal/code-editor-modal.module';
import { IonButton } from '@ionic/angular/standalone';

@NgModule({
  declarations: [ConsoleButtonComponent],
  exports: [ConsoleButtonComponent],
  imports: [
    CommonModule,
    BubbleModule,
    RouterLink,
    CodeEditorModalModule,
    IonButton,
  ],
})
export class ConsoleButtonModule {}
