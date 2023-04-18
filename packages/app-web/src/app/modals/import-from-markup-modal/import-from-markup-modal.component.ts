import { Component, Input, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';

type UrlExtractor = (markup: string) => string[];

export interface ImportFromMarkupModalComponentProps {
  kind: string;
  urlExtractor: UrlExtractor;
}

@Component({
  selector: 'app-import-from-markup-modal',
  templateUrl: './import-from-markup-modal.component.html',
  styleUrls: ['./import-from-markup-modal.component.scss'],
})
export class ImportFromMarkupModalComponent
  implements OnInit, ImportFromMarkupModalComponentProps
{
  @Input()
  kind: string;
  @Input()
  urlExtractor: UrlExtractor;

  constructor(private readonly modalCtrl: ModalController) {}

  ngOnInit() {}

  cancel() {
    return this.modalCtrl.dismiss();
  }

  importUrls() {}
}
