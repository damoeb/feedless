import { Component, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from './services/auth.service';
import { ProfileService } from './services/profile.service';
import { AlertController, ModalController } from '@ionic/angular';
import { AppUpdateService } from './services/app-update.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss'],
})
export class AppComponent implements OnDestroy {
  public appPages = [
    // { title: 'Articles', url: '/articles' },
    { title: 'Sources', url: '/buckets' },
    { title: 'Feeds', url: '/feeds' },
    { title: 'Profile', url: '/profile' },
  ];
  public metaPages = [
    { title: 'Privacy', url: '/privacy' },
    { title: 'Terms', url: '/terms' },
    { title: 'Contact', url: '/contact' },
  ];

  private subscriptions: Subscription[] = [];
  constructor(
    readonly activatedRoute: ActivatedRoute,
    readonly router: Router,
    readonly profileService: ProfileService,
    readonly authService: AuthService,
    readonly appUpdateService: AppUpdateService, // do not remove
    readonly alertCtrl: AlertController,
    readonly modalCtrl: ModalController
  ) {
    this.subscriptions.push(
      activatedRoute.queryParams.subscribe(async (queryParams) => {
        if (queryParams.token) {
          console.log('with token');
          await this.authService.handleAuthenticationToken(queryParams.token);
          await this.router.navigate([], {
            queryParams: {
              signup: null,
              token: null,
            },
            queryParamsHandling: 'merge',
          });
        } else {
          console.log('without token');
          await new Promise((resolve) => setTimeout(resolve, 200));
          await profileService.fetchProfile('network-only');
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
