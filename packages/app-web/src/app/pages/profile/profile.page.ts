import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { dateTimeFormat, SessionService } from '../../services/session.service';
import { AlertController, ToastController } from '@ionic/angular/standalone';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { createEmailFormControl } from '../../form-controls';
import { Subscription } from 'rxjs';
import { ServerConfigService } from '../../services/server-config.service';
import { ConnectedAppService } from '../../services/connected-app.service';
import {
  Product,
  RepositoryFull,
  Session,
  UserSecret,
} from '../../graphql/types';
import { ProductService } from '../../services/product.service';
import { first } from 'lodash-es';
import { AppConfigService } from '../../services/app-config.service';
import dayjs from 'dayjs';
import { GqlProductCategory } from '../../../generated/graphql';
import { RepositoryService } from '../../services/repository.service';
import { addIcons } from 'ionicons';
import { cardOutline, cloudDownloadOutline } from 'ionicons/icons';
import {
  IonRouterLink,
  IonRouterLinkWithHref,
} from '@ionic/angular/standalone';

@Component({
  selector: 'app-profile-page',
  templateUrl: './profile.page.html',
  styleUrls: ['./profile.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProfilePage implements OnInit, OnDestroy {
  protected secrets: UserSecret[] = [];
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
  protected product: Product;
  protected readonly dateTimeFormat = dateTimeFormat;
  private subscriptions: Subscription[] = [];
  private connectedApps: Session['user']['connectedApps'] = [];

  constructor(
    private readonly toastCtrl: ToastController,
    protected readonly sessionService: SessionService,
    protected readonly repositoryService: RepositoryService,
    protected readonly changeRef: ChangeDetectorRef,
    protected readonly productService: ProductService,
    protected readonly alertCtrl: AlertController,
    protected readonly connectedAppService: ConnectedAppService,
    private readonly appConfig: AppConfigService,
    protected readonly serverConfig: ServerConfigService,
  ) {
    addIcons({ cardOutline, cloudDownloadOutline });
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

  ngOnInit(): void {
    this.appConfig.setPageTitle('Profile');

    this.subscriptions.push(
      this.sessionService.getSession().subscribe(async (session) => {
        if (session.isLoggedIn) {
          this.connectedApps = session.user.connectedApps;
          this.secrets = session.user.secrets;

          this.product = first(
            await this.productService.listProducts({
              id: { eq: session.user.plan.productId },
            }),
          );

          this.formFg.patchValue({
            email: session.user.email,
            firstName: session.user.firstName,
            lastName: session.user.lastName,
            emailVerified: session.user.emailValidated,
          });
          this.changeRef.detectChanges();
        }
      }),
    );
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

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
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
      const apiToken = await this.sessionService.createUserSecret();
      this.secrets.push(apiToken);
    }
  }

  async deleteSecret(secret: UserSecret) {
    await this.sessionService.deleteUserSecret({
      where: {
        eq: secret.id,
      },
    });
  }

  async saveProfile() {
    if (this.formFg.valid && this.formFg.dirty) {
      await this.sessionService.updateCurrentUser({
        firstName: {
          set: this.formFg.value.firstName,
        },
        lastName: {
          set: this.formFg.value.lastName,
        },
        country: {
          set: this.formFg.value.country,
        },
        email: {
          set: this.formFg.value.email,
        },
      });

      const toast = await this.toastCtrl.create({
        message: 'Saved',
        duration: 3000,
        color: 'success',
      });

      await toast.present();
    }
  }

  async disconnectApp(appName: string) {
    await this.connectedAppService.deleteConnectedApp(
      this.getConnectedAppByName(appName).id,
    );
    location.reload();
  }

  hasConnectedApp(appName: string) {
    return !!this.getConnectedAppByName(appName);
  }

  private getConnectedAppByName(appName: string) {
    return this.connectedApps?.find((it) => it.app == appName);
  }

  async downloadAllRepositories() {
    const repos = await this.getAllRepositories();
    await this.repositoryService.downloadRepositories(
      repos,
      `feedless-full-backup-${dayjs().format('YYYY-MM-DD')}.json`,
    );
  }

  private async getAllRepositories(): Promise<RepositoryFull[]> {
    const repositories: RepositoryFull[] = [];

    let page = 0;
    while (true) {
      const repositoriesOnPage = await this.repositoryService.listRepositories({
        cursor: {
          page,
        },
        where: {
          product: {
            eq: GqlProductCategory.Feedless,
          },
        },
      });
      if (repositoriesOnPage.length === 0) {
        break;
      }
      for (let index = 0; index < repositoriesOnPage.length; index++) {
        repositories.push(
          await this.repositoryService.getRepositoryById(
            repositoriesOnPage[index].id,
          ),
        );
      }
      page++;
    }
    return repositories;
  }

  uploadRepositories(uploadEvent: Event) {
    const file = (uploadEvent.target as any).files[0];
    const reader = new FileReader();
    reader.onload = async (e) => {
      const data: ArrayBuffer | string = (e.target as any).result;
      const repositories: RepositoryFull[] = JSON.parse(String(data));

      // TODO await this.repositoryService.createRepositories(repositories)
    };
    reader.readAsText(file);
  }
}
