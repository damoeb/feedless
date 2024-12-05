import { Component, inject } from '@angular/core';
import { ModalController } from '@ionic/angular/standalone';
import { ContentType } from '../../elements/code-editor/code-editor.component';
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
  standalone: false,
})
export class CodeEditorModalComponent implements CodeEditorModalComponentProps {
  private readonly modalCtrl = inject(ModalController);

  text: string;
  title: string;
  contentType: ContentType;
  readOnly: boolean;
  controls: boolean;

  constructor() {
    addIcons({ closeOutline });
  }

  cancel() {
    return this.modalCtrl.dismiss();
  }
}
