import { Component } from '@angular/core';
import { ModalController } from '@ionic/angular/standalone';
import { LatLon } from '../../components/map/map.component';
import { addIcons } from 'ionicons';
import { closeOutline } from 'ionicons/icons';

export interface MapModalComponentProps {
  position: LatLon;
  perimeter: number;
}

@Component({
  selector: 'app-map-modal',
  templateUrl: './map-modal.component.html',
  styleUrls: ['./map-modal.component.scss'],
})
export class MapModalComponent implements MapModalComponentProps {
  position: LatLon;

  perimeter: number;

  constructor(private readonly modalCtrl: ModalController) {
    addIcons({ closeOutline });
  }

  closeModal() {
    return this.modalCtrl.dismiss(this.position);
  }

  handlePositionChange(latLon: any) {
    this.position = latLon;
  }
}
