import { Component } from '@angular/core';
import { ModalController } from '@ionic/angular';

@Component({
  selector: 'app-export-modal',
  templateUrl: './export-modal.component.html',
  styleUrls: ['./export-modal.component.scss']
})
export class ExportModalComponent {
  constructor(private readonly modalCtrl: ModalController) {
  }

  closeModal() {
    return this.modalCtrl.dismiss();
  }
}
