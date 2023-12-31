import { Component } from '@angular/core';
import { ModalController } from '@ionic/angular';

export interface AgentsModalComponentProps {}

@Component({
  selector: 'app-agents-modal',
  templateUrl: './agents-modal.component.html',
  styleUrls: ['./agents-modal.component.scss'],
})
export class AgentsModalComponent implements AgentsModalComponentProps {
  constructor(private readonly modalCtrl: ModalController) {}

  dismissModal() {
    return this.modalCtrl.dismiss();
  }

  applyChanges() {
    return this.modalCtrl.dismiss({});
  }
}
