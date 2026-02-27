import { Component, inject, input, PLATFORM_ID } from '@angular/core';
import { NamedLatLon } from '@feedless/core';
import {
  ActionSheetController,
  IonButton,
  ModalController,
} from '@ionic/angular/standalone';
import {
  AppConfigService,
  IconComponent,
  ServerConfigService,
} from '@feedless/components';
import { addIcons } from 'ionicons';
import { notificationsOutline } from 'ionicons/icons';
import {
  SubmitModalComponent,
  SubmitModalComponentProps,
} from '../submit-modal/submit-modal.component';
import { SubmitModalModule } from '../submit-modal/submit-modal.module';
import {
  GqlRecordOrderByInput,
  GqlRecordsWhereInput,
  GqlSortOrder,
} from '@feedless/graphql-api';
import { GeoService } from '@feedless/geo';
import { isPlatformBrowser } from '@angular/common';

type SubscriptionType = 'cal' | 'atom';

@Component({
  selector: 'app-search-abo-button',
  templateUrl: './search-abo-button.component.html',
  styleUrls: ['./search-abo-button.component.scss'],
  imports: [IonButton, IconComponent, SubmitModalModule, IonButton],
  standalone: true,
})
export class SearchAboButtonComponent {
  private readonly modalCtrl = inject(ModalController);
  private readonly appConfigService = inject(AppConfigService);
  private readonly geoService = inject(GeoService);
  private readonly actionSheetCtrl = inject(ActionSheetController);
  private readonly serverConfig = inject(ServerConfigService);
  private readonly platformId = inject(PLATFORM_ID);

  readonly location = input.required<NamedLatLon>();
  readonly perimeter = input<number>();
  protected isBrowser = isPlatformBrowser(this.platformId);

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({
        notificationsOutline,
      });
    }
  }

  private getRepositoryId(): string {
    return this.appConfigService.customProperties['eventRepositoryId'] as any;
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

  useUsersPosition() {
    if (isPlatformBrowser(this.platformId)) {
      this.geoService.requestLocationFromBrowser();
    }
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
          text: 'Kalender Import (iCal)',
          handler: () => this.createCalenderOrFeedSubscription('cal'),
        },
        {
          icon: 'logo-rss',
          text: 'RSS Feed',
          handler: () => this.createCalenderOrFeedSubscription('atom'),
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

  private createCalenderOrFeedSubscription(type: SubscriptionType) {
    const currentLocation = this.location();
    if (!currentLocation) {
      return;
    }
    const where: GqlRecordsWhereInput = {
      repository: {
        id: this.getRepositoryId(),
      },
      startedAt: {
        inFuture: true,
      },
      latLng: {
        near: {
          point: {
            lat: currentLocation.lat,
            lng: currentLocation.lng,
          },
          distanceKm: this.perimeter(),
        },
      },
    };

    const orderBy: GqlRecordOrderByInput = {
      startedAt: GqlSortOrder.Asc,
    };

    const url = `${this.serverConfig.apiUrl}/f/${this.getRepositoryId()}/${type}?where=${encodeURIComponent(JSON.stringify(where))}&orderBy=${encodeURIComponent(JSON.stringify(orderBy))}`;
    console.log(`Subscribing ${type}`, url);

    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('target', '_blank');
    link.click();
  }
}
