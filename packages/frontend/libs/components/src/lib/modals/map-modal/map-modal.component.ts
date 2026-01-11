import { Component, inject, PLATFORM_ID } from '@angular/core';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
  ModalController,
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { closeOutline } from 'ionicons/icons';
import { LatLng } from '@feedless/core';
import { MapComponent } from '../../components/map/map.component';
import { isPlatformBrowser } from '@angular/common';
import { IconComponent } from '../../components/icon/icon.component';

export interface MapModalComponentProps {
  position: LatLng;
  perimeter: number;
}

@Component({
  selector: 'app-map-modal',
  templateUrl: './map-modal.component.html',
  styleUrls: ['./map-modal.component.scss'],
  standalone: true,
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IconComponent,
    IonContent,
    MapComponent,
  ],
})
export class MapModalComponent implements MapModalComponentProps {
  private readonly modalCtrl = inject(ModalController);
  private readonly platformId = inject(PLATFORM_ID);

  position: LatLng;

  perimeter: number;

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({ closeOutline });
    }
  }

  closeModal() {
    return this.modalCtrl.dismiss(this.position);
  }

  handlePositionChange(latLon: any) {
    this.position = latLon;
  }
}
