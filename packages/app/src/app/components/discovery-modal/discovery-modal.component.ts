import { Component } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { ModalCancel, ModalSuccess } from '../../app.module';
import {
  TransientGenericFeedAndDiscovery,
  TransientNativeFeedAndDiscovery
} from '../feed-discovery-wizard/feed-discovery-wizard.component';

export interface DiscoveryModalComponentProps {
  url: string;
}

export interface DiscoveryPayload {
  genericFeedAndDiscovery?: TransientGenericFeedAndDiscovery;
  nativeFeed?: TransientNativeFeedAndDiscovery;
}

export interface DiscoveryModalSuccess extends ModalSuccess {
  data: DiscoveryPayload;
}

@Component({
  selector: 'app-discovery-modal',
  templateUrl: './discovery-modal.component.html',
  styleUrls: ['./discovery-modal.component.scss'],
})
export class DiscoveryModalComponent implements DiscoveryModalComponentProps {
  url: string;

  constructor(private readonly modalCtrl: ModalController) {}

  async saveGeneric(genericFeedAndDiscovery: TransientGenericFeedAndDiscovery) {
    const response: DiscoveryModalSuccess = {
      cancel: false,
      data: { genericFeedAndDiscovery },
    };
    await this.modalCtrl.dismiss(response);
  }

  async saveNative(event: TransientNativeFeedAndDiscovery) {
    const response: DiscoveryModalSuccess = {
      cancel: false,
      data: { nativeFeed: event },
    };
    await this.modalCtrl.dismiss(response);
  }

  async closeModal() {
    const response: ModalCancel = {
      cancel: true,
    };

    await this.modalCtrl.dismiss(response);
  }
}
