import { Component } from '@angular/core';
import { ModalController } from '@ionic/angular/standalone';
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

export interface SubmitModalComponentProps {
  repositoryId: string;
}

type ReportFrequency = 'week' | 'month';

@Component({
  selector: 'app-tags-modal',
  templateUrl: './submit-modal.component.html',
  styleUrls: ['./submit-modal.component.scss'],
})
export class SubmitModalComponent implements SubmitModalComponentProps {
  repositoryId: string;
  reportFrequencyWeek: ReportFrequency = 'week';
  reportFrequencyMonth: ReportFrequency = 'month';

  protected formGroup = new FormGroup({
    frequency: new FormControl<ReportFrequency>('week'),
    email: createEmailFormControl(''),
    name: new FormControl<string>('', Validators.minLength(3)),
  });

  constructor(
    private readonly modalCtrl: ModalController,
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
    if (this.formGroup.valid) {
      await this.reportService.createReport(this.repositoryId, {
        what: {
          tags: {},
          latLng: {
            distanceKm: 10,
            near: {
              lat: 0, // todo this.location.lat,
              lon: 0, // todo this.location.lon,
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
    }
  }
}
