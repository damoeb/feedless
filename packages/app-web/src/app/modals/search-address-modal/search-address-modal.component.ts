import { Component } from '@angular/core';
import { ModalController } from '@ionic/angular/standalone';
import { ModalCancel } from '../../app.module';
import {
  OpenStreetMapService,
  OsmMatch,
} from '../../services/open-street-map.service';
import { GqlGeoPoint } from '../../../generated/graphql';
import { NamedLatLon } from '../../products/upcoming/places';
import { addIcons } from 'ionicons';
import { closeOutline } from 'ionicons/icons';

@Component({
  selector: 'app-search-address-modal',
  templateUrl: './search-address-modal.component.html',
  styleUrls: ['./search-address-modal.component.scss'],
})
export class SearchAddressModalComponent {
  matches: NamedLatLon[];
  loading = false;
  protected latLon: GqlGeoPoint;

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly openStreetMapService: OpenStreetMapService,
  ) {
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

  async pick(match: Pick<NamedLatLon, 'lat' | 'lon'>) {
    await this.modalCtrl.dismiss(match);
  }

  private parsePoint(query: string): GqlGeoPoint | undefined {
    try {
      const parts = query.trim().split(/[, ]+/);
      const lat = parseFloat(parts[0]);
      const lon = parseFloat(parts[1]);
      if (!isNaN(lat) && !isNaN(lon)) {
        return { lat, lon };
      }
    } catch (e) {}
  }

  pickLatLon() {
    return this.pick({
      lat: this.latLon.lat,
      lon: this.latLon.lon,
    });
  }
}
