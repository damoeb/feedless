import { Component } from '@angular/core';
import { ModalController } from '@ionic/angular';

export interface ModalModalComponentProps {
  // tags: string[];
}

@Component({
  selector: 'app-buy-modal',
  templateUrl: './buy-modal.component.html',
  styleUrls: ['./buy-modal.component.scss'],
})
export class BuyModalComponent implements ModalModalComponentProps {

  constructor(private readonly modalCtrl: ModalController) {}

  closeModal() {
    return this.modalCtrl.dismiss();
  }

}
