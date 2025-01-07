import { Component, inject } from '@angular/core';
import { ModalController } from '@ionic/angular/standalone';
import { ModalCancel } from '../../app.module';
import { addIcons } from 'ionicons';
import { closeOutline } from 'ionicons/icons';

export interface DataTableModalProps {}

@Component({
  selector: 'app-data-table-modal',
  templateUrl: './data-table-modal.component.html',
  styleUrls: ['./data-table-modal.component.scss'],
  standalone: false,
})
export class DataTableModalComponent implements DataTableModalProps {
  private readonly modalCtrl = inject(ModalController);

  loading = false;

  constructor() {
    addIcons({ closeOutline });
  }

  async closeModal() {
    const response: ModalCancel = {
      cancel: true,
    };

    await this.modalCtrl.dismiss(response);
  }
}
