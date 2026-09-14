import { Component, inject, PLATFORM_ID } from '@angular/core';
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
// eslint-disable-next-line @nx/enforce-module-boundaries
import { IconComponent, ReportService } from '@feedless/components';
import { ReportSubscriptionFormComponent } from '../report-subscription-form/report-subscription-form.component';
import {
  ReportSubscriptionValue,
  toSegmentInput,
} from '../report-subscription-form/report-subscription';

export interface EmailAboModalComponentProps {
  repositoryId: string;
  location: Nullable<NamedLatLon>;
}

/**
 * Das Modal, in dem ein anonymer Besucher ein Abo anlegt.
 *
 * Nur noch ein Container: das Formular liegt in
 * ReportSubscriptionFormComponent, die Übersetzung in toSegmentInput. Hier
 * bleibt, was an das Modal gebunden ist - den Ort aus der Seite übergeben, den
 * Service aufrufen, schliessen und melden.
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
      // Das Modal bleibt offen, damit die Eingabe nicht verloren geht.
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
