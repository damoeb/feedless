import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { WizardContext } from '../wizard/wizard/wizard.component';
import { ModalController, ToastController } from '@ionic/angular';
import { Authentication, AuthService } from '../../services/auth.service';
import { ProfileService } from '../../services/profile.service';
import { Router } from '@angular/router';
import { ImporterService } from '../../services/importer.service';
import { WizardService } from '../../services/wizard.service';
import { Subscription } from 'rxjs';
import { Profile } from '../../graphql/types';

@Component({
  selector: 'app-page-header',
  templateUrl: './page-header.component.html',
  styleUrls: ['./page-header.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PageHeaderComponent implements OnInit, OnDestroy {
  @Input()
  showNotifications = true;
  @Input()
  showTitle = true;
  authorization: Authentication;
  profile: Profile;

  private subscriptions: Subscription[] = [];
  darkMode: boolean;

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly profileService: ProfileService,
    private readonly wizardService: WizardService,
    private readonly importerService: ImporterService,
    private readonly toastCtrl: ToastController,
    private readonly changeRef: ChangeDetectorRef,
    private readonly router: Router,
    private readonly authService: AuthService,
  ) {}

  async ngOnInit(): Promise<void> {
    this.subscriptions.push(
      this.profileService.getProfile().subscribe((profile) => {
        this.profile = profile;
        this.changeRef.detectChanges();
      }),
      this.authService
        .authorizationChange()
        .subscribe(async (authorization) => {
          this.authorization = authorization;
          this.changeRef.detectChanges();
        }),
      this.profileService.watchColorScheme().subscribe((isDarkMode) => {
        this.darkMode = isDarkMode;
        this.changeRef.detectChanges();
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  hasPendingWizardState(): boolean {
    return this.wizardService.hasPendingWizardState();
  }

  async resumeWizard() {
    const wizardContext: Partial<WizardContext> =
      this.wizardService.getPendingWizardState();
    this.deletePendingWizardState();
    await this.wizardService.openFeedWizard(wizardContext);
    await this.changeRef.detectChanges();
  }

  deletePendingWizardState() {
    this.wizardService.deletePendingWizardState();
  }

  refresh() {
    this.changeRef.detectChanges();
  }

  async restoreAccount() {
    await this.profileService.updateCurrentUser({
      purgeScheduledFor: {
        assignNull: true,
      },
    });
  }

  toggleColorScheme() {
    this.profileService.setColorScheme(!this.darkMode);
  }
}
