import { Component, inject, input, OnDestroy, OnInit } from '@angular/core';
import { NamedLatLon } from '../../../types';
import {
  ActionSheetController,
  AlertController,
  IonButton,
  IonFooter,
  IonIcon,
  ModalController,
} from '@ionic/angular/standalone';
import { AppConfigService } from '../../../services/app-config.service';
import { addIcons } from 'ionicons';
import {
  calendarNumberOutline,
  closeOutline,
  heart,
  locateOutline,
  logoRss,
  mailOutline,
  sendOutline,
} from 'ionicons/icons';
import {
  SubmitModalComponent,
  SubmitModalComponentProps,
} from '../submit-modal/submit-modal.component';
import { GeoService } from '../../../services/geo.service';
import { Subscription } from 'rxjs';

import { RemoveIfProdDirective } from '../../../directives/remove-if-prod/remove-if-prod.directive';
import { RouterLink } from '@angular/router';
import { SubmitModalModule } from '../submit-modal/submit-modal.module';

@Component({
  selector: 'app-upcoming-footer',
  templateUrl: './upcoming-footer.component.html',
  styleUrls: ['./upcoming-footer.component.scss'],
  imports: [
    IonFooter,
    IonButton,
    IonIcon,
    RemoveIfProdDirective,
    RouterLink,
    SubmitModalModule,
  ],
  standalone: true,
})
export class UpcomingFooterComponent implements OnInit, OnDestroy {
  private readonly modalCtrl = inject(ModalController);
  private readonly alertCtrl = inject(AlertController);
  private readonly appConfigService = inject(AppConfigService);
  private readonly geoService = inject(GeoService);
  private readonly actionSheetCtrl = inject(ActionSheetController);

  readonly location = input<NamedLatLon>();

  private subscriptions: Subscription[] = [];

  constructor() {
    addIcons({
      sendOutline,
      heart,
      locateOutline,
      mailOutline,
      calendarNumberOutline,
      logoRss,
      closeOutline,
    });
  }

  private getRepositoryId(): string {
    return this.appConfigService.customProperties.eventRepositoryId as any;
  }

  async createMailSubscription() {
    const componentProps: SubmitModalComponentProps = {
      repositoryId: this.getRepositoryId(),
      location: this.location(),
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

  async createSubscription() {
    const actionSheet = await this.actionSheetCtrl.create({
      header: 'Veranstaltungen abonieren',
      buttons: [
        {
          icon: 'mail-outline',
          text: 'Email Abo',
          handler: () => this.createMailSubscription(),
        },
        {
          icon: 'calendar-number-outline',
          text: 'Calendar Import (iCal)',
        },
        {
          icon: 'logo-rss',
          text: 'RSS Feed',
        },
        {
          icon: 'close-outline',
          text: 'Abbrechen',
          role: 'cancel',
          data: {
            action: 'cancel',
          },
        },
      ],
    });

    await actionSheet.present();
  }
}
