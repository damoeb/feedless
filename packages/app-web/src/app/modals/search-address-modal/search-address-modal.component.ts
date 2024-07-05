import { Component } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { ModalCancel } from '../../app.module';
import { OpenStreetMapService, OsmMatch } from '../../services/open-street-map.service';


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
    this.matches = await this.openStreetMapService.searchAddress(query);
    this.loading = false;
  }

  async pick(match: OsmMatch) {
    await this.modalCtrl.dismiss(match);
  }
}
