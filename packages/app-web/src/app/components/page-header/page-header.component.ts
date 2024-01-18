import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ModalController, ToastController } from '@ionic/angular';
import { Authentication, AuthService } from '../../services/auth.service';
import { ProfileService } from '../../services/profile.service';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { Profile } from '../../graphql/types';
import { ModalService } from '../../services/modal.service';

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
    private readonly modalService: ModalService,
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
    return this.modalService.hasPendingWizardState();
  }

  async resumeWizard() {
    await this.modalService.resumeFeedWizard();
    await this.changeRef.detectChanges();
  }

  deletePendingWizardState() {
    this.modalService.resetWizardState();
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
