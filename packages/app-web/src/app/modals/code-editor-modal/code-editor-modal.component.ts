import { Component, OnInit, inject } from '@angular/core';
import {
  ModalController,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonButtons,
  IonButton,
  IonIcon,
  IonContent,
} from '@ionic/angular/standalone';
import {
  ContentType,
  CodeEditorComponent,
} from '../../elements/code-editor/code-editor.component';
import { addIcons } from 'ionicons';
import { closeOutline } from 'ionicons/icons';

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
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IonIcon,
    IonContent,
    CodeEditorComponent,
  ],
  standalone: true,
})
export class CodeEditorModalComponent
  implements OnInit, CodeEditorModalComponentProps
{
  private readonly modalCtrl = inject(ModalController);

  text: string;
  title: string;
  contentType: ContentType;
  readOnly: boolean;
  controls: boolean;

  constructor() {
    addIcons({ closeOutline });
  }

  ngOnInit() {}

  cancel() {
    return this.modalCtrl.dismiss();
  }
}
