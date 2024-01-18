import { Component } from '@angular/core';
import { ModalController, ToastController } from '@ionic/angular';

export interface GenerateFeedModalComponentProps {}

@Component({
  selector: 'app-generate-feed-modal',
  templateUrl: './generate-feed-modal.component.html',
  styleUrls: ['./generate-feed-modal.component.scss'],
})
export class GenerateFeedModalComponent
  implements GenerateFeedModalComponentProps
{
  atomFeedUrl: string = 'https://feedless.org/f/234234234';
  jsonFeedUrl: string = 'https://feedless.org/f/2342342346';
  constructor(
    private readonly modalCtrl: ModalController,
    private readonly toastCtrl: ToastController,
  ) {}
  closeModal() {
    return this.modalCtrl.dismiss();
  }

  async copy(jsonFeedUrl: string) {
    const toast = await this.toastCtrl.create({
      message: 'Link copied',
      duration: 3000,
    });

    await toast.present();
  }
}
