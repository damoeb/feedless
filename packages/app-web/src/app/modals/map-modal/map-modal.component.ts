import { Component, inject } from '@angular/core';
import {
  ModalController,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonButtons,
  IonButton,
  IonIcon,
  IonContent,
} from '@ionic/angular/standalone';
import { LatLon, MapComponent } from '../../components/map/map.component';
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
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IonIcon,
    IonContent,
    MapComponent,
  ],
  standalone: true,
})
export class MapModalComponent implements MapModalComponentProps {
  private readonly modalCtrl = inject(ModalController);

  position: LatLon;

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
