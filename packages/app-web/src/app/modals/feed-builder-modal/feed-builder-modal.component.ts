import { Component } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { ScrapeSourceModalComponent } from '../scrape-source-modal/scrape-source-modal.component';

@Component({
  selector: 'app-export-modal',
  templateUrl: './feed-builder-modal.component.html',
  styleUrls: ['./feed-builder-modal.component.scss'],
})
export class FeedBuilderModalComponent {

  constructor(private readonly modalCtrl: ModalController) {}

  closeModal() {
    return this.modalCtrl.dismiss();
  }

  async openScrapeSourceModal() {
    const modal = await this.modalCtrl.create({
      component: ScrapeSourceModalComponent,
      showBackdrop: true,
    });
    await modal.present();
    const scrapeRequest = await modal.onDidDismiss()
  }
}
