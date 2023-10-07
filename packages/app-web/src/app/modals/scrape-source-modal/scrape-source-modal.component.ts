import { Component } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { GqlScrapeRequestInput } from '../../../generated/graphql';

@Component({
  selector: 'app-scrape-source-modal',
  templateUrl: './scrape-source-modal.component.html',
  styleUrls: ['./scrape-source-modal.component.scss'],
})
export class ScrapeSourceModalComponent {
  private source: GqlScrapeRequestInput;
  constructor(private readonly modalCtrl: ModalController) {}

  closeModal() {
    return this.modalCtrl.dismiss();
  }

  handleSourceChanged(source: GqlScrapeRequestInput) {
    this.source = source;
  }

  save() {
    return this.modalCtrl.dismiss(this.source)
  }
}
