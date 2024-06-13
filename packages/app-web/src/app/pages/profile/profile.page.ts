import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { SessionService } from '../../services/session.service';
import { ToastController } from '@ionic/angular';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { createEmailFormControl } from '../../form-controls';
import { Subscription } from 'rxjs';
import { ServerConfigService } from '../../services/server-config.service';

@Component({
  selector: 'app-profile-page',
  templateUrl: './profile.page.html',
  styleUrls: ['./profile.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProfilePage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];

  protected formFg = new FormGroup({
    email: createEmailFormControl<string>(''),
    country: new FormControl<string>('', Validators.required),
    firstName: new FormControl<string>('', [Validators.required, Validators.minLength(2)]),
    lastName: new FormControl<string>('', [Validators.required, Validators.minLength(2)]),
  });


  constructor(
    private readonly toastCtrl: ToastController,
    protected readonly sessionService: SessionService,
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
    this.subscriptions.push(
      this.sessionService.getSession().subscribe(session => {
        if (session.isLoggedIn) {
          this.formFg.patchValue({
            email: session.user.email,
            firstName: session.user.firstName,
            lastName: session.user.lastName,
          })
        }
      })
    )
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
}
