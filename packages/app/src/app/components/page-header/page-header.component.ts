import { Component, Input, OnInit } from '@angular/core';
import {
  WizardComponent,
  WizardComponentProps,
  WizardContext,
} from '../wizard/wizard/wizard.component';
import { ModalController, ToastController } from '@ionic/angular';
import { Authentication, AuthService } from '../../services/auth.service';
import { ProfileService } from '../../services/profile.service';
import { Router } from '@angular/router';
import { ImporterService } from '../../services/importer.service';

@Component({
  selector: 'app-page-header',
  templateUrl: './page-header.component.html',
  styleUrls: ['./page-header.component.scss'],
})
export class PageHeaderComponent implements OnInit {
  @Input()
  showNotifications = true;
  authorization: Authentication;
  private readonly unfinishedWizardKey = 'unfinished-wizard';

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly profileService: ProfileService,
    private readonly importerService: ImporterService,
    private readonly toastCtrl: ToastController,
    private readonly router: Router,
    private readonly authService: AuthService
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
        await this.importerService.createImporter({
          bucket: data.bucket,
          feed: data.feed,
          email: data.importer?.email,
          webhook: data.importer?.webhook,
          filter: data.importer?.filter,
          autoRelease: data.importer?.autoRelease,
        });
        const toast = await this.toastCtrl.create({
          message: 'Feed Created',
          duration: 3000,
          color: 'success',
        });

        await toast.present();
        break;
    }
  }

  async ngOnInit(): Promise<void> {
    this.authService.authorizationChange().subscribe(async (authorization) => {
      this.authorization = authorization;
      // console.log('authorization', authorization);
    });
  }

  async handleProfileAction(event: any) {
    switch (event.detail.value) {
      case 'profile':
        await this.router.navigateByUrl('/profile');
        break;
      case 'logout':
        await this.profileService.logout();
        await this.router.navigateByUrl('/login');
        break;
    }
  }

  hasPendingWizardState(): boolean {
    return localStorage.getItem(this.unfinishedWizardKey)?.length > 0;
  }

  deletePendingWizardState(): void {
    localStorage.removeItem(this.unfinishedWizardKey);
  }

  async resumeWizard() {
    const wizardContext: Partial<WizardContext> = JSON.parse(
      localStorage.getItem(this.unfinishedWizardKey)
    );
    // this.deletePendingWizardState();
    await this.openFeedWizard(wizardContext);
  }

  private saveWizardContext(data: WizardContext): void {
    localStorage.setItem(this.unfinishedWizardKey, JSON.stringify(data));
  }
}
