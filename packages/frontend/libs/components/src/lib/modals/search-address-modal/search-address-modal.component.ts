import { Component, inject, PLATFORM_ID } from '@angular/core';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonItem,
  IonLabel,
  IonList,
  IonTitle,
  IonToolbar,
  ModalController,
} from '@ionic/angular/standalone';
import { GqlGeoPoint } from '@feedless/graphql-api';
import { addIcons } from 'ionicons';
import { closeOutline } from 'ionicons/icons';
import { NamedLatLon, Nullable } from '@feedless/core';
import { SearchbarComponent } from '@feedless/form-elements';
import { OpenStreetMapService } from '@feedless/geo';
import { isPlatformBrowser } from '@angular/common';
import { IconComponent } from '../../components/icon/icon.component';

@Component({
  selector: 'app-search-address-modal',
  templateUrl: './search-address-modal.component.html',
  styleUrls: ['./search-address-modal.component.scss'],
  standalone: true,
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IconComponent,
    IonContent,
    SearchbarComponent,
    IonList,
    IonItem,
    IonLabel,
  ],
})
export class SearchAddressModalComponent {
  private readonly modalCtrl = inject(ModalController);
  private readonly openStreetMapService = inject(OpenStreetMapService);
  private readonly platformId = inject(PLATFORM_ID);

  matches: NamedLatLon[];
  loading = false;
  protected latLon: GqlGeoPoint;

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({ closeOutline });
    }
  }

  async closeModal() {
    const response = {
      cancel: true,
    };

    await this.modalCtrl.dismiss(response);
  }

  async searchAddress(query: string) {
    this.loading = true;
    this.latLon = this.parsePoint(query);
    this.matches = await this.openStreetMapService.searchByQuery(query);
    this.loading = false;
  }

  async pick(match: Pick<NamedLatLon, 'lat' | 'lng'>) {
    await this.modalCtrl.dismiss(match);
  }

  private parsePoint(query: string): Nullable<GqlGeoPoint> {
    try {
      const parts = query.trim().split(/[, ]+/);
      const lat = parseFloat(parts[0]);
      const lng = parseFloat(parts[1]);
      if (!isNaN(lat) && !isNaN(lng)) {
        return { lat, lng };
      }
    } catch (e) {
      // ignore
    }
    return null;
  }

  pickLatLon() {
    return this.pick({
      lat: this.latLon.lat,
      lng: this.latLon.lng,
    });
  }
}
