import { Component, inject, input, PLATFORM_ID } from '@angular/core';
import { NamedLatLon } from '@feedless/core';
import { AlertController, IonButton } from '@ionic/angular/standalone';
import { AppConfigService } from '@feedless/components';
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
import { RouterLink } from '@angular/router';
import { SubmitModalModule } from '../submit-modal/submit-modal.module';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-upcoming-footer',
  templateUrl: './upcoming-footer.component.html',
  styleUrls: ['./upcoming-footer.component.scss'],
  imports: [IonButton, RouterLink, SubmitModalModule, IonButton],
  standalone: true,
})
export class UpcomingFooterComponent {
  private readonly alertCtrl = inject(AlertController);
  private readonly appConfigService = inject(AppConfigService);
  private readonly platformId = inject(PLATFORM_ID);

  readonly location = input<NamedLatLon>();
  readonly perimeter = input<number>(10);
  protected isBrowser = isPlatformBrowser(this.platformId);

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
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
          value: this.appConfigService.customProperties['operatorName'],
        },
        {
          label: 'Adresse',
          attributes: {
            readonly: true,
          },
          type: 'text',
          value: this.appConfigService.customProperties['operatorAddress'],
        },
        {
          label: 'Email',
          attributes: {
            readonly: true,
          },
          type: 'text',
          value: this.appConfigService.customProperties['operatorEmail'],
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
}
