import { Component, Input } from '@angular/core';
import { NamedLatLon } from '../../../types';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { createEmailFormControl } from '../../../form-controls';
import {
  GqlFeedlessPlugins,
  GqlIntervalUnit,
} from '../../../../generated/graphql';
import dayjs from 'dayjs';
import { ReportService } from '../../../services/report.service';
import { AlertController, ModalController } from '@ionic/angular/standalone';
import { AppConfigService } from '../../../services/app-config.service';
import { addIcons } from 'ionicons';
import { sendOutline } from 'ionicons/icons';
import { FinalizeProfileModalComponent } from '../../../modals/finalize-profile-modal/finalize-profile-modal.component';
import {
  SubmitModalComponent,
  SubmitModalComponentProps,
} from '../submit-modal/submit-modal.component';

@Component({
  selector: 'app-upcoming-footer',
  templateUrl: './upcoming-footer.component.html',
  styleUrls: ['./upcoming-footer.component.scss'],
})
export class UpcomingFooterComponent {
  @Input()
  location: NamedLatLon;

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly appConfigService: AppConfigService,
  ) {
    addIcons({ sendOutline });
  }

  private getRepositoryId(): string {
    return this.appConfigService.customProperties.eventRepositoryId as any;
  }

  async createMailSubscription(name: string = '', email: string = '') {
    const componentProps: SubmitModalComponentProps = {
      repositoryId: this.getRepositoryId(),
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
}
