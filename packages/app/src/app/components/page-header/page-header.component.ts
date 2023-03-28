import { Component, Input, OnInit } from '@angular/core';
import { WizardContext } from '../wizard/wizard/wizard.component';
import { ModalController, ToastController } from '@ionic/angular';
import { Authentication, AuthService } from '../../services/auth.service';
import { ProfileService } from '../../services/profile.service';
import { Router } from '@angular/router';
import { ImporterService } from '../../services/importer.service';
import { WizardService } from '../../services/wizard.service';

@Component({
  selector: 'app-page-header',
  templateUrl: './page-header.component.html',
  styleUrls: ['./page-header.component.scss'],
})
export class PageHeaderComponent implements OnInit {
  @Input()
  showNotifications = true;
  authorization: Authentication;

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly profileService: ProfileService,
    private readonly wizardService: WizardService,
    private readonly importerService: ImporterService,
    private readonly toastCtrl: ToastController,
    private readonly router: Router,
    private readonly authService: AuthService
  ) {}

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
    return this.wizardService.hasPendingWizardState();
  }

  async resumeWizard() {
    const wizardContext: Partial<WizardContext> =
      this.wizardService.getPendingWizardState();
    // this.deletePendingWizardState();
    await this.wizardService.openFeedWizard(wizardContext);
  }

  deletePendingWizardState() {
    this.wizardService.deletePendingWizardState();
  }
}
