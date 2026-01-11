import { Component, inject, PLATFORM_ID } from '@angular/core';
import { AlertController, ModalController } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import {
  bodyOutline,
  closeOutline,
  mailOutline,
  sendOutline,
  trashOutline,
} from 'ionicons/icons';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { createEmailFormControl, NamedLatLon, Nullable } from '@feedless/core';
import { GqlFeedlessPlugins, GqlIntervalUnit } from '@feedless/graphql-api';
import dayjs from 'dayjs';
import { ReportService } from '@feedless/services';
import { isPlatformBrowser } from '@angular/common';

export interface SubmitModalComponentProps {
  repositoryId: string;
  location: Nullable<NamedLatLon>;
}

type ReportFrequency = 'week' | 'month';

@Component({
  selector: 'app-submit-modal',
  templateUrl: './submit-modal.component.html',
  styleUrls: ['./submit-modal.component.scss'],
  standalone: false,
})
export class SubmitModalComponent implements SubmitModalComponentProps {
  private readonly modalCtrl = inject(ModalController);
  private readonly alertCtrl = inject(AlertController);
  private readonly reportService = inject(ReportService);
  private readonly platformId = inject(PLATFORM_ID);

  repositoryId: string;
  location: NamedLatLon;
  reportFrequencyWeek: ReportFrequency = 'week';
  reportFrequencyMonth: ReportFrequency = 'month';

  protected formGroup = new FormGroup({
    frequency: new FormControl<ReportFrequency>('week'),
    acceptedTerms: new FormControl<boolean>(false, Validators.requiredTrue),
    email: createEmailFormControl(''),
    name: new FormControl<string>('', [
      Validators.minLength(3),
      Validators.required,
    ]),
  });

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({
        closeOutline,
        trashOutline,
        mailOutline,
        bodyOutline,
        sendOutline,
      });
    }
  }

  closeModal() {
    return this.modalCtrl.dismiss();
  }

  async createMailSubscription() {
    Object.values<FormControl>(this.formGroup.controls).forEach((fc) =>
      fc.markAllAsTouched(),
    );
    if (this.formGroup.valid) {
      await this.reportService.createReport(this.repositoryId, {
        what: {
          tags: {},
          latLng: {
            near: {
              point: {
                lat: this.location.lat,
                lng: this.location.lng,
              },
              distanceKm: 10,
            },
          },
        },
        when: {
          scheduled: {
            interval: GqlIntervalUnit.Week,
            startingAt: dayjs().day(0).toDate().getTime(),
          },
        },
        report: {
          plugin: {
            pluginId: GqlFeedlessPlugins.OrgFeedlessEventReport,
            params: {},
          },
        },
        recipient: {
          email: {
            email: this.formGroup.value.email,
            name: this.formGroup.value.name,
          },
        },
      });
      await this.modalCtrl.dismiss();
      const alert = await this.alertCtrl.create({
        header: 'Gratis Email-Abo erstellt!',
        backdropDismiss: true,
        message: `Wir senden dir bald eine Bestätigungmail.`,
        buttons: [
          {
            text: 'OK',
            role: 'confirm',
          },
        ],
      });
      await alert.present();
    }
  }
}
