import { Injectable } from '@angular/core';
import { ModalController, ToastController } from '@ionic/angular';
import {
  WizardComponent,
  WizardComponentProps,
  WizardContext,
} from '../components/wizard/wizard/wizard.component';
import { ImporterService } from './importer.service';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class WizardService {
  private readonly unfinishedWizardKey = 'unfinished-wizard';

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly router: Router,
    private readonly importerService: ImporterService,
    private readonly toastCtrl: ToastController
  ) {}

  async openFeedWizard(initialContext: Partial<WizardContext> = {}) {
    const componentProps: WizardComponentProps = {
      initialContext,
    };
    const modal = await this.modalCtrl.create({
      component: WizardComponent,
      componentProps,
      showBackdrop: true,
      backdropDismiss: false,
    });
    await modal.present();
    const { data, role } = await modal.onDidDismiss<WizardContext>();
    switch (role) {
      case 'cancel':
        this.saveWizardContext(data);
        break;
      case 'login':
        this.saveWizardContext(data);
        await this.router.navigateByUrl('/login');
        break;
      case 'persist':
        await this.importerService.createImporters({
          bucket: data.bucket,
          feeds: [data.feed],
          email: data.importer?.email,
          webhook: data.importer?.webhook,
          filter: data.importer?.filter,
          autoRelease: data.importer?.autoRelease,
        });
        // await this.importerService.createImporter({
        //   bucket: data.bucket,
        //   feed: data.feed,
        //   email: data.importer?.email,
        //   webhook: data.importer?.webhook,
        //   filter: data.importer?.filter,
        //   autoRelease: data.importer?.autoRelease,
        // });
        const toast = await this.toastCtrl.create({
          message: 'Feed Created',
          duration: 3000,
          color: 'success',
        });

        await toast.present();
        break;
    }
  }

  hasPendingWizardState(): boolean {
    return localStorage.getItem(this.unfinishedWizardKey)?.length > 0;
  }

  getPendingWizardState(): Partial<WizardContext> {
    return JSON.parse(localStorage.getItem(this.unfinishedWizardKey));
  }

  deletePendingWizardState() {
    localStorage.removeItem(this.unfinishedWizardKey);
  }

  private saveWizardContext(data: WizardContext): void {
    localStorage.setItem(this.unfinishedWizardKey, JSON.stringify(data));
  }
}
