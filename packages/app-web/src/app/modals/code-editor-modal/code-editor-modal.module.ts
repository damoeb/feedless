import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CodeEditorModalComponent } from './code-editor-modal.component';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonIcon,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';
import { CodeEditorComponent } from '../../elements/code-editor/code-editor.component';

@NgModule({
  declarations: [CodeEditorModalComponent],
  exports: [CodeEditorModalComponent],
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    IonHeader,
    ReactiveFormsModule,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IonIcon,
    IonContent,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IonIcon,
    IonContent,
    CodeEditorComponent,
  ],
})
export class CodeEditorModalModule {}
