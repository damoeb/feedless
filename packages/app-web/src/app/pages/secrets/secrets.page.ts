import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { dateTimeFormat, SessionService } from '../../services/session.service';
import { Router } from '@angular/router';
import { UserSecret } from '../../graphql/types';
import { AlertController, ToastController } from '@ionic/angular';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-secrets-page',
  templateUrl: './secrets.page.html',
  styleUrls: ['./secrets.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SecretsPage implements OnInit, OnDestroy {
  secrets: UserSecret[] = [];
  private subscriptions: Subscription[] = [];

  protected readonly dateTimeFormat = dateTimeFormat;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly alertCtrl: AlertController,
    private readonly profileService: SessionService,
  ) {}

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  ngOnInit(): void {
    this.subscriptions.push(
      this.profileService.getSession().subscribe((profile) => {
        if (profile.user.secrets) {
          this.secrets = profile.user.secrets;
        }
        this.changeRef.detectChanges();
      }),
    );
  }

  async createUserSecret() {
    const promptName = await this.alertCtrl.create({
      message: 'Set a name for the key',
      inputs: [
        {
          name: 'name',
          type: 'text',
          min: 3,
          placeholder: 'Type name here',
        },
      ],
      buttons: [
        {
          role: 'cancel',
          text: 'Cancel',
        },
        {
          text: 'Create Secret Key',
          role: 'persist',
        },
      ],
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
        in: [secret.id],
      },
    });
  }
}
