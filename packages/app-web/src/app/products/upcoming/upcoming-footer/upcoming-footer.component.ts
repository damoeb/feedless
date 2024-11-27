import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { NamedLatLon } from '../../../types';
import { AlertController, ModalController } from '@ionic/angular/standalone';
import { AppConfigService } from '../../../services/app-config.service';
import { addIcons } from 'ionicons';
import {
  alert,
  heart,
  locateOutline,
  pinOutline,
  sendOutline,
} from 'ionicons/icons';
import {
  SubmitModalComponent,
  SubmitModalComponentProps,
} from '../submit-modal/submit-modal.component';
import { GeoService } from '../../../services/geo.service';
import { Subscription } from 'rxjs';
import { homeRoute } from '../upcoming-product-routing.module';
import dayjs from 'dayjs';

@Component({
    selector: 'app-upcoming-footer',
    templateUrl: './upcoming-footer.component.html',
    styleUrls: ['./upcoming-footer.component.scss'],
    standalone: false
})
export class UpcomingFooterComponent implements OnInit, OnDestroy {
  @Input()
  location: NamedLatLon;

  private subscriptions: Subscription[] = [];

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly alertCtrl: AlertController,
    private readonly appConfigService: AppConfigService,
    private readonly geoService: GeoService,
  ) {
    addIcons({ sendOutline, heart, locateOutline });
  }

  private getRepositoryId(): string {
    return this.appConfigService.customProperties.eventRepositoryId as any;
  }

  async createMailSubscription() {
    const componentProps: SubmitModalComponentProps = {
      repositoryId: this.getRepositoryId(),
      location: this.location,
    };
    const modal = await this.modalCtrl.create({
      component: SubmitModalComponent,
      componentProps,
      cssClass: 'medium-modal',
      backdropDismiss: false,
    });
    await modal.present();
    await modal.onDidDismiss();
  }

  async showAttribution() {
    const alert = await this.alertCtrl.create({
      header: 'Impressum',
      backdropDismiss: false,
      message:
        'Dies ist ein privates Hobbyprojekt um den vielen kleinen Veranstaltungen mehr Sichtbarkeit zu geben und den Usern eine' +
        'vereinfachte Möglichkeiten zu geben, diese nicht mehr zu verpassen.',
      inputs: [
        {
          label: 'Betreiber',
          attributes: {
            readonly: true,
          },
          type: 'text',
          value: this.appConfigService.customProperties.operatorName,
        },
        {
          label: 'Adresse',
          attributes: {
            readonly: true,
          },
          type: 'text',
          value: this.appConfigService.customProperties.operatorAddress,
        },
        {
          label: 'Email',
          attributes: {
            readonly: true,
          },
          type: 'text',
          value: this.appConfigService.customProperties.operatorEmail,
        },
      ],
      buttons: [
        {
          role: 'cancel',
          text: 'Schliessen',
        },
      ],
    });
    await alert.present();
  }

  ngOnInit(): void {
    this.subscriptions.push(
      this.geoService.getCurrentLatLon().subscribe((location) => {}),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  // getPlaceUrl(location: NamedLatLon): string {
  //   if (location) {
  //     const now = dayjs();
  //     const { countryCode, area, place } = location;
  //     return (
  //       '/' +
  //       homeRoute({})
  //         .countryCode({ countryCode })
  //         .region({ region: area })
  //         .events({
  //           place,
  //           perimeter: 10,
  //           year: parseInt(now.format('YYYY')),
  //           month: parseInt(now.format('MM')),
  //           day: parseInt(now.format('DD')),
  //         }).$
  //     );
  //   }
  // }

  useUsersPosition() {
    this.geoService.requestLocationFromBrowser();
  }
}
