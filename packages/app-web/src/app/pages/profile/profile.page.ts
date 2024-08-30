import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { SessionService } from '../../services/session.service';
import { ToastController } from '@ionic/angular';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { createEmailFormControl } from '../../form-controls';
import { Subscription } from 'rxjs';
import { ServerConfigService } from '../../services/server-config.service';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-profile-page',
  templateUrl: './profile.page.html',
  styleUrls: ['./profile.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProfilePage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];

  protected formFg = new FormGroup({
    email: createEmailFormControl<string>(''),
    emailVerified: new FormControl<boolean>(false),
    country: new FormControl<string>('', [
      Validators.minLength(2),
      Validators.maxLength(50),
    ]),
    firstName: new FormControl<string>('', [
      Validators.minLength(2),
      Validators.maxLength(50),
    ]),
    lastName: new FormControl<string>('', [
      Validators.minLength(2),
      Validators.maxLength(50),
    ]),
  });

  constructor(
    private readonly toastCtrl: ToastController,
    protected readonly sessionService: SessionService,
    private readonly titleService: Title,
    protected readonly serverConfig: ServerConfigService,
  ) {}

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

  ngOnInit(): void {
    this.titleService.setTitle('Profile');
    this.subscriptions.push(
      this.sessionService.getSession().subscribe((session) => {
        if (session.isLoggedIn) {
          this.formFg.patchValue({
            email: session.user.email,
            firstName: session.user.firstName,
            lastName: session.user.lastName,
            emailVerified: session.user.emailValidated,
          });
        }
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
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

  async saveProfile() {
    if (this.formFg.valid && this.formFg.dirty) {
      await this.sessionService.updateCurrentUser({
        firstName: {
          set: this.formFg.value.firstName
        },
        lastName: {
          set: this.formFg.value.lastName
        },
        country: {
          set: this.formFg.value.country
        },
        email: {
          set: this.formFg.value.email
        }
      });

      const toast = await this.toastCtrl.create({
        message: 'Saved',
        duration: 3000,
        color: 'success',
      });

      await toast.present();
    }
  }
}
