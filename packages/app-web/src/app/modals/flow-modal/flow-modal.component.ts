import { Component, inject } from '@angular/core';
import { ModalController } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { chevronBackOutline } from 'ionicons/icons';

export interface FlowModalComponentProps {
  // tags: string[];
}

@Component({
  selector: 'app-flow-modal',
  templateUrl: './flow-modal.component.html',
  styleUrls: ['./flow-modal.component.scss'],
  standalone: false,
})
export class FlowModalComponent implements FlowModalComponentProps {
  private readonly modalCtrl = inject(ModalController);

  constructor() {
    addIcons({ chevronBackOutline });
  }

  closeModal() {
    return this.modalCtrl.dismiss();
  }
}
