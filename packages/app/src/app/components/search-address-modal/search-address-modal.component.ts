import { Component } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { ModalCancel, ModalSuccess } from '../../app.module';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

// https://nominatim.openstreetmap.org/search?q=Innsbruck&format=json&addressdetails=1
interface OsmMatch {
  lat: string;
  lon: string;
  // eslint-disable-next-line @typescript-eslint/naming-convention
  display_name: 'Innsbruck, Tyrol, Austria';
  importance: number;
}

export interface SearchAddressPayload {
  lat: string;
  lon: string;
}

export interface SearchAddressModalSuccess extends ModalSuccess {
  data: SearchAddressPayload;
}

@Component({
  selector: 'app-search-address-modal',
  templateUrl: './search-address-modal.component.html',
  styleUrls: ['./search-address-modal.component.scss'],
})
export class SearchAddressModalComponent {
  query = '';
  matches: OsmMatch[];
  loading = false;

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly httpClient: HttpClient
  ) {}

  async closeModal() {
    const response: ModalCancel = {
      cancel: true,
    };

    await this.modalCtrl.dismiss(response);
  }

  async searchAddress() {
    this.loading = true;
    const url = `https://nominatim.openstreetmap.org/search?q=${this.query}&format=json&polygon=1&addressdetails=1`;
    this.matches = await firstValueFrom(this.httpClient.get<OsmMatch[]>(url));
    this.loading = false;
  }

  async pick(match: OsmMatch) {
    const response: SearchAddressModalSuccess = {
      cancel: false,
      data: { lat: match.lat, lon: match.lon },
    };
    await this.modalCtrl.dismiss(response);
  }
}
