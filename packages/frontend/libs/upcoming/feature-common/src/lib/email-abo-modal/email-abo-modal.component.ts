import { Component, inject, PLATFORM_ID, ChangeDetectionStrategy } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import {
  AlertController,
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
  ModalController,
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { closeOutline } from 'ionicons/icons';
import { NamedLatLon, Nullable } from '@feedless/core';
 
import { IconComponent } from '@feedless/ui';
import { ReportService } from '@feedless/data-access';
import { ReportSubscriptionFormComponent } from '@feedless/upcoming-ui';
import {
  ReportSubscriptionValue,
  toSegmentInput,
} from '@feedless/upcoming-ui';

export interface EmailAboModalComponentProps {
  repositoryId: string;
  location: Nullable<NamedLatLon>;
}

/**
 * The modal in which an anonymous visitor creates a subscription.
 *
 * Just a container now: the form lives in ReportSubscriptionFormComponent,
 * the translation in toSegmentInput. What stays here is what's bound to the
 * modal - passing in the page's location, calling the service, closing and
 * reporting the outcome.
 */
@Component({
  selector: 'app-email-abo-modal',
  templateUrl: './email-abo-modal.component.html',
  styleUrls: ['./email-abo-modal.component.scss'],
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IonContent,
    IconComponent,
    ReportSubscriptionFormComponent,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: true,
})
export class EmailAboModalComponent implements EmailAboModalComponentProps {
  private readonly modalCtrl = inject(ModalController);
  private readonly alertCtrl = inject(AlertController);
  private readonly reportService = inject(ReportService);
  private readonly platformId = inject(PLATFORM_ID);

  repositoryId: string;
  location: NamedLatLon;

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({ closeOutline });
    }
  }

  closeModal() {
    return this.modalCtrl.dismiss();
  }

  async subscribe(value: ReportSubscriptionValue): Promise<void> {
    try {
      await this.reportService.createReport(
        this.repositoryId,
        toSegmentInput(value, this.location, Date.now()),
      );
    } catch (e) {
      // The modal stays open so the input isn't lost.
      console.error('createReport failed', e);
      await this.showAlert(
        'Das hat nicht geklappt',
        'Dein Abo konnte gerade nicht angelegt werden. Versuche es bitte später noch einmal.',
      );
      return;
    }

    await this.modalCtrl.dismiss();
    // One text for every outcome, so the answer never reveals whether this address must confirm first.
    await this.showAlert('Danke!', 'Wir haben dir eine E-Mail geschickt.');
  }

  private async showAlert(header: string, message: string): Promise<void> {
    const alert = await this.alertCtrl.create({
      header,
      message,
      backdropDismiss: true,
      buttons: [{ text: 'OK', role: 'confirm' }],
    });
    await alert.present();
  }
}
