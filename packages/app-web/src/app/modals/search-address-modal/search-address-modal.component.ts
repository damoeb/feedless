import { Component } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { ModalCancel, ModalSuccess } from '../../app.module';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

// https://nominatim.openstreetmap.org/search?q=Innsbruck&format=json&addressdetails=1
export interface OsmMatch {
  lat: string;
  lon: string;
  display_name: string;
  importance: number;
}

@Component({
  selector: 'app-search-address-modal',
  templateUrl: './search-address-modal.component.html',
  styleUrls: ['./search-address-modal.component.scss'],
})
export class SearchAddressModalComponent {
  matches: OsmMatch[];
  loading = false;

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly httpClient: HttpClient,
  ) {}

  async closeModal() {
    const response: ModalCancel = {
      cancel: true,
    };

    await this.modalCtrl.dismiss(response);
  }

  async searchAddress(query: string) {
    this.loading = true;
    const url = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(
      query,
    )}&format=json&polygon=1&addressdetails=1`;
    this.matches = await firstValueFrom(this.httpClient.get<OsmMatch[]>(url));
    this.loading = false;
  }

  async pick(match: OsmMatch) {
    await this.modalCtrl.dismiss(match);
  }
}
