import { Component } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { LatLon } from '../../components/map/map.component';

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

  constructor(private readonly modalCtrl: ModalController) {}

  closeModal() {
    return this.modalCtrl.dismiss(this.position);
  }

  handlePositionChange(latLon: any) {
    this.position = latLon;
  }
}
