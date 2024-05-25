import { Component } from '@angular/core';
import { SessionService } from '../../services/session.service';
import { Router } from '@angular/router';
import { ToastController } from '@ionic/angular';

@Component({
  selector: 'app-profile-page',
  templateUrl: './profile.page.html',
  styleUrls: ['./profile.page.scss'],
})
export class ProfilePage {
  constructor(
    private readonly router: Router,
    private readonly toastCtrl: ToastController,
    private readonly sessionService: SessionService,
  ) {}

  async logout() {
    await this.sessionService.logout();
    await this.router.navigateByUrl('/');
  }

  async deleteAccount() {
    await this.sessionService.updateCurrentUser({
      purgeScheduledFor: {
        assignNull: false,
      },
    });
    const toast = await this.toastCtrl.create({
      message: 'Account deletion scheduled',
      duration: 3000,
      color: 'success',
    });

    await toast.present();
  }

  // private async updatePluginValue(id: string, value: boolean) {
  //   await this.profileService.updateCurrentUser({
  //     plugins: [
  //       {
  //         id,
  //         value: {
  //           set: value,
  //         },
  //       },
  //     ],
  //   });
  // }
}
