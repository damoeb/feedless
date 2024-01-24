import { Component, Input, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { FormControl } from '@angular/forms';

export interface CodeEditorModalComponentProps {
  code: string;
}

@Component({
  selector: 'app-code-editor-modal',
  templateUrl: './code-editor-modal.component.html',
  styleUrls: ['./code-editor-modal.component.scss']
})
export class CodeEditorModalComponent
  implements CodeEditorModalComponentProps, OnInit {
  @Input()
  code: string;

  codeFC: FormControl<string | null>;

  constructor(private readonly modalCtrl: ModalController) {
  }

  dismissModal() {
    return this.modalCtrl.dismiss();
  }

  applyChanges() {
    return this.modalCtrl.dismiss({});
  }

  ngOnInit(): void {
    this.codeFC = new FormControl<string>(this.code);
  }
}
