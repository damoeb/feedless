import { Component } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { ModalCancel } from '../../app.module';
import {
  OpenStreetMapService,
  OsmMatch,
} from '../../services/open-street-map.service';
import { GqlGeoPoint } from '../../../generated/graphql';

@Component({
  selector: 'app-search-address-modal',
  templateUrl: './search-address-modal.component.html',
  styleUrls: ['./search-address-modal.component.scss'],
})
export class SearchAddressModalComponent {
  matches: OsmMatch[];
  loading = false;
  protected latLon: GqlGeoPoint;

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly openStreetMapService: OpenStreetMapService,
  ) {}

  async closeModal() {
    const response: ModalCancel = {
      cancel: true,
    };

    await this.modalCtrl.dismiss(response);
  }

  async searchAddress(query: string) {
    this.loading = true;
    this.latLon = this.parsePoint(query);
    this.matches = await this.openStreetMapService.searchAddress(query);
    this.loading = false;
  }

  async pick(match: OsmMatch) {
    await this.modalCtrl.dismiss(match);
  }

  private parsePoint(query: string): GqlGeoPoint|undefined {
    try {
      const parts = query.trim().split(/[, ]+/);
      return {
        lat: parseFloat(parts[0]),
        lon: parseFloat(parts[1]),
      }
    } catch (e) {}
  }

  pickLatLon() {
    return this.pick({
      lat: `${this.latLon.lat}`,
      lon: `${this.latLon.lon}`,
    })
  }
}
