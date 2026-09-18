import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonIcon,
  IonTitle,
  IonToolbar,
  ModalController,
} from '@ionic/angular/standalone';
import { CodeEditorComponent, ContentType } from '../../elements/code-editor/code-editor.component';
import { addIcons } from 'ionicons';
import { closeOutline } from 'ionicons/icons';

import { FormsModule, ReactiveFormsModule } from '@angular/forms';

export interface CodeEditorModalComponentProps {
  text: string;
  title: string;
  readOnly?: boolean;
  controls?: boolean;
  contentType?: ContentType;
}

@Component({
  selector: 'app-code-editor-modal',
  templateUrl: './code-editor-modal.component.html',
  styleUrls: ['./code-editor-modal.component.scss'],
  standalone: true,
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [
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
export class CodeEditorModalComponent implements CodeEditorModalComponentProps {
  private readonly modalCtrl = inject(ModalController);

  text: string;
  title: string;
  contentType: ContentType;
  readOnly: boolean;

  constructor() {
    addIcons({ closeOutline });
  }

  cancel() {
    return this.modalCtrl.dismiss();
  }
}
