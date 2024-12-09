import { Component, inject } from '@angular/core';
import { ModalController } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { closeOutline } from 'ionicons/icons';
import { LatLng } from '../../types';

export interface MapModalComponentProps {
  position: LatLng;
  perimeter: number;
}

@Component({
  selector: 'app-map-modal',
  templateUrl: './map-modal.component.html',
  styleUrls: ['./map-modal.component.scss'],
  standalone: false,
})
export class MapModalComponent implements MapModalComponentProps {
  private readonly modalCtrl = inject(ModalController);

  position: LatLng;

  perimeter: number;

  constructor() {
    addIcons({ closeOutline });
  }

  closeModal() {
    return this.modalCtrl.dismiss(this.position);
  }

  handlePositionChange(latLon: any) {
    this.position = latLon;
  }
}
