import { Component, Input, OnInit } from '@angular/core';
import { WizardComponent, WizardComponentProps, WizardContext } from '../wizard/wizard/wizard.component';
import { ModalController } from '@ionic/angular';
import { Authentication, AuthService } from '../../services/auth.service';
import { ProfileService } from '../../services/profile.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-page-header',
  templateUrl: './page-header.component.html',
  styleUrls: ['./page-header.component.scss'],
})
export class PageHeaderComponent implements OnInit {
  authorization: Authentication;
  @Input()
  showNotifications = true;
  private readonly unfinishedWizardKey = 'unfinished-wizard';

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly profileService: ProfileService,
    private readonly router: Router,
    private readonly authService: AuthService
  ) {}

  async openFeedWizard(initialContext: Partial<WizardContext> = {}) {
    const componentProps: WizardComponentProps = {
      initialContext
    };
    const modal = await this.modalCtrl.create({
      component: WizardComponent,
      componentProps,
      showBackdrop: false,
    });
    await modal.present();
    const dismissal = await modal.onDidDismiss<WizardContext>();
    console.log(dismissal.role);
    switch (dismissal.role) {
      case 'login':
        this.saveWizardContext(dismissal.data);
        await this.router.navigateByUrl('/login');
        break;
      case 'persist': break;
    }
  }

  async ngOnInit(): Promise<void> {
    this.authService.authorizationChange().subscribe(async (authorization) => {
      this.authorization = authorization;
      console.log('authorization', authorization);
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
    const wizardContext: Partial<WizardContext> = JSON.parse(localStorage.getItem(this.unfinishedWizardKey));
    // this.deletePendingWizardState();
    await this.openFeedWizard(wizardContext);
  }

  private saveWizardContext(data: WizardContext): void {
    localStorage.setItem(this.unfinishedWizardKey, JSON.stringify(data));
  }
}
