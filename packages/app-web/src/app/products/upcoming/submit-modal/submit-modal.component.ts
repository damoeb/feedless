import { Component } from '@angular/core';
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
import { createEmailFormControl } from '../../../form-controls';
import {
  GqlFeedlessPlugins,
  GqlIntervalUnit,
} from '../../../../generated/graphql';
import dayjs from 'dayjs';
import { ReportService } from '../../../services/report.service';
import { NamedLatLon } from '../../../types';

export interface SubmitModalComponentProps {
  repositoryId: string;
  location: NamedLatLon;
}

type ReportFrequency = 'week' | 'month';

@Component({
  selector: 'app-submit-modal',
  templateUrl: './submit-modal.component.html',
  styleUrls: ['./submit-modal.component.scss'],
  standalone: false,
})
export class SubmitModalComponent implements SubmitModalComponentProps {
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

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly alertCtrl: AlertController,
    private readonly reportService: ReportService,
  ) {
    addIcons({
      closeOutline,
      trashOutline,
      mailOutline,
      bodyOutline,
      sendOutline,
    });
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
            distanceKm: 10,
            near: {
              lat: this.location.lat,
              lon: this.location.lon,
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
