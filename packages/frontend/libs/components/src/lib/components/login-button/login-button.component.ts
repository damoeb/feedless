import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  input,
  OnDestroy,
  OnInit,
  PLATFORM_ID,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { Authentication, AuthService, SessionService } from '../../services';

import { IonButton } from '@ionic/angular/standalone';
import { RouterLink } from '@angular/router';
import { addIcons } from 'ionicons';
import { logOutOutline } from 'ionicons/icons';
import { isPlatformBrowser } from '@angular/common';
import { IconComponent } from '../icon/icon.component';

@Component({
  selector: 'app-login-button',
  templateUrl: './login-button.component.html',
  styleUrls: ['./login-button.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonButton, RouterLink, IconComponent],
  standalone: true,
})
export class LoginButtonComponent implements OnInit, OnDestroy {
  private readonly authService = inject(AuthService);
  protected readonly sessionService = inject(SessionService);
  private readonly changeRef = inject(ChangeDetectorRef);

  authorization: Authentication;
  private subscriptions: Subscription[] = [];

  readonly label = input<string>();
  readonly expand = input<string>();
  readonly color = input<string>();
  private readonly platformId = inject(PLATFORM_ID);

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({ logOutOutline });
    }
  }

  async ngOnInit(): Promise<void> {
    this.subscriptions.push(
      this.authService
        .authorizationChange()
        .subscribe(async (authorization) => {
          this.authorization = authorization;
          this.changeRef.detectChanges();
        }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  logout() {
    return this.sessionService.logout();
  }
}
