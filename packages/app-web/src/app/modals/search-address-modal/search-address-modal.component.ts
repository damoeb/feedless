import { Component, inject } from '@angular/core';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonTitle,
  IonToolbar,
  ModalController,
} from '@ionic/angular/standalone';
import { ModalCancel } from '../../app.module';
import { OpenStreetMapService } from '../../services/open-street-map.service';
import { GqlGeoPoint } from '../../../generated/graphql';
import { addIcons } from 'ionicons';
import { closeOutline } from 'ionicons/icons';
import { NamedLatLon } from '../../types';
import { SearchbarComponent } from '../../elements/searchbar/searchbar.component';

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
    IonIcon,
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

  matches: NamedLatLon[];
  loading = false;
  protected latLon: GqlGeoPoint;

  constructor() {
    addIcons({ closeOutline });
  }

  async closeModal() {
    const response: ModalCancel = {
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

  private parsePoint(query: string): GqlGeoPoint | undefined {
    try {
      const parts = query.trim().split(/[, ]+/);
      const lat = parseFloat(parts[0]);
      const lng = parseFloat(parts[1]);
      if (!isNaN(lat) && !isNaN(lng)) {
        return { lat, lng };
      }
    } catch (e) {}
  }

  pickLatLon() {
    return this.pick({
      lat: this.latLon.lat,
      lng: this.latLon.lng,
    });
  }
}
