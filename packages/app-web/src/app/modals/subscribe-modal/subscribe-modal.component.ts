import { Component, OnInit } from '@angular/core';
import { ModalController, ToastController } from '@ionic/angular';

export interface SubscribeModalComponentProps {
  atomFeedUrl: string;
  jsonFeedUrl: string;
  filter?: object;
}

@Component({
  selector: 'app-subscribe-modal',
  templateUrl: './subscribe-modal.component.html',
  styleUrls: ['./subscribe-modal.component.scss'],
})
export class SubscribeModalComponent
  implements OnInit, SubscribeModalComponentProps
{
  atomFeedUrl: string;
  jsonFeedUrl: string;
  filter: object;

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly toastCtrl: ToastController
  ) {}
  ngOnInit() {}

  cancel() {
    return this.modalCtrl.dismiss();
  }

  async copy(jsonFeedUrl: string) {
    const toast = await this.toastCtrl.create({
      message: 'Link copied',
      duration: 3000,
      color: 'success',
    });

    await toast.present();
  }
}
