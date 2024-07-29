import { Component, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';

export interface CodeEditorModalComponentProps {
  element: string;
}

@Component({
  selector: 'app-import-opml',
  templateUrl: './code-editor-modal.component.html',
  styleUrls: ['./code-editor-modal.component.scss'],
})
export class CodeEditorModalComponent
  implements OnInit, CodeEditorModalComponentProps
{
  element: string;

  constructor(private readonly modalCtrl: ModalController) {}

  ngOnInit() {}

  cancel() {
    return this.modalCtrl.dismiss();
  }
}
