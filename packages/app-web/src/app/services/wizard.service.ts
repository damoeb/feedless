import { Injectable } from '@angular/core';
import { ModalController, ToastController } from '@ionic/angular';
import {
  WizardComponent,
  WizardComponentProps,
  WizardContext,
  WizardExistRole,
} from '../components/wizard/wizard/wizard.component';
import { ImporterService } from './importer.service';
import { Router } from '@angular/router';
import { FeedService } from './feed.service';

@Injectable({
  providedIn: 'root',
})
export class WizardService {
  private readonly unfinishedWizardKey = 'unfinished-wizard';

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly router: Router,
    private readonly importerService: ImporterService,
    private readonly feedService: FeedService,
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
      case WizardExistRole.login:
        this.saveWizardContext(data);
        await this.router.navigateByUrl('/login');
        break;
      case WizardExistRole.persistFeed:
        await this.feedService.createNativeFeed(data.feed.create);
        await this.showToast();
        break;
      case WizardExistRole.persistBucket:
        await this.importerService.createImporters({
          bucket: data.bucket,
          feeds: [data.feed],
          filter: data.importer?.filter,
          autoRelease: data.importer?.autoRelease,
        });
        await this.showToast();
        break;
      default:
        this.saveWizardContext(data);
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

  private async showToast() {
    const toast = await this.toastCtrl.create({
      message: 'Feed Created',
      duration: 3000,
      color: 'success',
    });

    await toast.present();
  }
}
