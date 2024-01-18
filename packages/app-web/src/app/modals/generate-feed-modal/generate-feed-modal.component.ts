import { Component } from '@angular/core';
import { ModalController } from '@ionic/angular';

export interface GenerateFeedModalComponentProps {
}

@Component({
  selector: 'app-generate-feed-modal',
  templateUrl: './generate-feed-modal.component.html',
  styleUrls: ['./generate-feed-modal.component.scss'],
})
export class GenerateFeedModalComponent implements GenerateFeedModalComponentProps {
  constructor(private readonly modalCtrl: ModalController) {}

  closeModal() {
    return this.modalCtrl.dismiss();
  }
}
