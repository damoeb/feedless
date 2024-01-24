import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { dateFormat, dateTimeFormat, ProfileService } from '../../services/profile.service';
import { Router } from '@angular/router';
import { UserSecret } from '../../graphql/types';
import { AlertController, ModalController, ToastController } from '@ionic/angular';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.page.html',
  styleUrls: ['./profile.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProfilePage implements OnInit, OnDestroy {
  secrets: UserSecret[] = [];
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly router: Router,
    private readonly toastCtrl: ToastController,
    private readonly modalCtrl: ModalController,
    private readonly alertCtrl: AlertController,
    private readonly profileService: ProfileService
  ) {
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  ngOnInit(): void {
    this.subscriptions.push(
      this.profileService.getProfile().subscribe((profile) => {
        if (profile.user.secrets) {
          this.secrets = profile.user.secrets;
        }
        // this.plugins = profile.user.plugins.map((plugin) => {
        //   const formControl = new FormControl<boolean>(plugin.value);
        //
        //   formControl.valueChanges.subscribe((value) =>
        //     this.updatePluginValue(plugin.id, value),
        //   );
        //   return {
        //     plugin,
        //     fc: formControl,
        //   };
        // });
        this.changeRef.detectChanges();
      })
    );
  }

  async logout() {
    await this.profileService.logout();
    await this.router.navigateByUrl('/');
  }

  async createUserSecret() {
    const promptName = await this.alertCtrl.create({
      message: 'Set a name for the key',
      inputs: [
        {
          name: 'name',
          type: 'text',
          min: 3,
          placeholder: 'Type name here'
        }
      ],
      buttons: [
        {
          role: 'cancel',
          text: 'Cancel'
        },
        {
          text: 'Create Secret Key',
          role: 'persist'
        }
      ]
    });
    await promptName.present();
    const data = await promptName.onDidDismiss();
    if (data.role === 'persist' && data.data.values.name) {
      const apiToken = await this.profileService.createUserSecret();
      this.secrets.push(apiToken);
    }
  }

  async deleteSecret(secret: UserSecret) {
    await this.profileService.deleteUserSecrets({
      where: {
        in: [secret.id]
      }
    });
  }

  async deleteAccount() {
    await this.profileService.updateCurrentUser({
      purgeScheduledFor: {
        assignNull: false
      }
    });
    const toast = await this.toastCtrl.create({
      message: 'Account deletion scheduled',
      duration: 3000,
      color: 'success'
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
  protected readonly dateTimeFormat = dateTimeFormat;
}
