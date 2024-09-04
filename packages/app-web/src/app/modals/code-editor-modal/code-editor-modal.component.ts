import { Component, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { ContentType } from '../../elements/code-editor/code-editor.component';

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
})
export class CodeEditorModalComponent
  implements OnInit, CodeEditorModalComponentProps
{
  text: string;
  title: string;
  contentType: ContentType;
  readOnly: boolean;
  controls: boolean;

  constructor(private readonly modalCtrl: ModalController) {}

  ngOnInit() {}

  cancel() {
    return this.modalCtrl.dismiss();
  }
}
