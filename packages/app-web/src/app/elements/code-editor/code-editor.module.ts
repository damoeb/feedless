import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CodeEditorComponent } from './code-editor.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  IonButton,
  IonButtons,
  IonIcon,
  IonToolbar,
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [CodeEditorComponent],
  exports: [CodeEditorComponent],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    IonToolbar,
    IonButtons,
    IonButton,
    IonIcon,
  ],
})
export class CodeEditorModule {}
