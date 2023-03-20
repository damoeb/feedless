import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from './services/auth.service';
import { ProfileService } from './services/profile.service';
import { ModalController } from '@ionic/angular';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss'],
})
export class AppComponent {
  public appPages = [
    { title: 'Buckets', url: '/buckets' },
    { title: 'Feeds', url: '/feeds' },
    { title: 'Settings', url: '/settings' },
    { title: 'Profile', url: '/profile' },
  ];
  constructor(
    readonly activatedRoute: ActivatedRoute,
    readonly router: Router,
    readonly profileService: ProfileService,
    readonly authService: AuthService,
    readonly modalCtrl: ModalController
  ) {
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
        await profileService.fetchProfile();
      }
    });
  }
}
