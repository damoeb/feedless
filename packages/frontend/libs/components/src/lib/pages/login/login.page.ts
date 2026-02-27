import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  isDevMode,
  OnDestroy,
  OnInit,
} from '@angular/core';
import {
  AppConfigService,
  AuthService,
  ServerConfigService,
} from '../../services';
import { ActivatedRoute, Router } from '@angular/router';
import { debounce, interval, Subscription } from 'rxjs';
import { addIcons } from 'ionicons';
import { logoGithub } from 'ionicons/icons';
import {
  IonButton,
  IonCard,
  IonCardContent,
  IonContent,
  IonIcon,
  IonInput,
  IonItem,
  IonLabel,
  IonList,
  IonSpinner,
} from '@ionic/angular/standalone';

import { FormsModule } from '@angular/forms';
import { EmailLoginComponent } from '../../components/email-login/email-login.component';
import { GqlAuthType, GqlProfileName } from '@feedless/graphql-api';

@Component({
  selector: 'app-login-page',
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonContent,
    IonSpinner,
    FormsModule,
    IonCardContent,
    IonList,
    IonItem,
    IonInput,
    IonLabel,
    EmailLoginComponent,
    IonCard,
    IonButton,
    IonIcon,
  ],
  standalone: true,
})
export class LoginPage implements OnInit, OnDestroy {
  protected readonly serverConfig = inject(ServerConfigService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly appConfig = inject(AppConfigService);
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly authService = inject(AuthService);

  showMailTokenForm: boolean;
  showOauth: boolean;
  loginUrl: string;
  showNoSignupBanner: boolean;
  showUserPasswordLogin: boolean;
  errorMessage: string;
  loading = true;
  private subscriptions: Subscription[] = [];

  constructor() {
    const serverConfig = this.serverConfig;

    // one sso provider for dev, per-product on prod
    if (isDevMode()) {
      this.loginUrl = serverConfig.apiUrl + '/oauth2/authorization/github';
    } else {
      this.loginUrl = `${serverConfig.apiUrl}/oauth2/authorization/${this.appConfig.activeProductConfig.id}`;
    }
    addIcons({ logoGithub });
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async ngOnInit() {
    this.appConfig.setPageTitle('Login');
    if (this.serverConfig.hasProfile(GqlProfileName.SelfHosted)) {
      this.showNoSignupBanner = false;
      this.showUserPasswordLogin = true;
    } else {
      this.showNoSignupBanner = true;
    }

    const redirectUrl = this.activatedRoute.snapshot.queryParams['redirectUrl'];
    if (redirectUrl) {
      this.authService.rememberRedirectUrl(redirectUrl);
    }

    this.subscriptions.push(
      this.authService
        .isAuthenticated()
        .pipe(debounce(() => interval(500)))
        .subscribe(async (authenticated) => {
          console.log('authenticated', authenticated);
          if (authenticated) {
            const { queryParams } = this.activatedRoute.snapshot;
            const { redirectUrl } = queryParams;
            const url = this.router.createUrlTree([redirectUrl || '/'], {});
            await this.router.navigateByUrl(url);
          } else {
            this.showOauth = this.serverConfig.hasAuthType(GqlAuthType.Oauth);
            this.showMailTokenForm = this.serverConfig.hasAuthType(
              GqlAuthType.MailToken,
            );
            this.loading = false;
            this.changeRef.detectChanges();
          }
        }),
    );
  }

  async loginWithUserPassword(
    email: string | number,
    password: string | number,
  ) {
    this.errorMessage = null;
    try {
      return await this.authService.authorizeUser({
        email: `${email}`,
        secretKey: `${password}`,
      });
    } catch (e: any) {
      this.errorMessage = e?.message;
      this.changeRef.detectChanges();
    }
  }
}
