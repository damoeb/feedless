import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from './services/auth.service';
import { ProfileService } from './services/profile.service';
import { AlertController, ModalController } from '@ionic/angular';
import { AppUpdateService } from './services/app-update.service';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss'],
})
export class AppComponent {
  public appPages = [
    { title: 'Buckets', url: '/buckets' },
    { title: 'Feeds', url: '/feeds' },
    { title: 'Profile', url: '/profile' },
  ];
  constructor(
    readonly activatedRoute: ActivatedRoute,
    readonly router: Router,
    readonly profileService: ProfileService,
    readonly authService: AuthService,
    readonly appUpdateService: AppUpdateService, // do not remove
    readonly alertCtrl: AlertController,
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
        await profileService.fetchProfile('network-only');
      }
    });

    this.showDevWarning();
  }

  private async showDevWarning() {
    const devWarningAccepted = localStorage.getItem('devWarningAccepted');
    if (!devWarningAccepted) {
      const alert = await this.alertCtrl.create({
        header: 'Development Notice',
        backdropDismiss: false,
        message:
          'This platform is under active development, so expect bugs. ' +
          'If you run into issues or have ideas, please report them on github!',
        buttons: [
          {
            text: 'Understood',
            role: 'confirm',
            handler: () => {
              localStorage.setItem('devWarningAccepted', 'true');
            },
          },
        ],
      });

      await alert.present();
    }
  }
}
