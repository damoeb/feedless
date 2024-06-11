import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ServerConfigService } from '../../services/server-config.service';
import { GqlProfileName } from '../../../generated/graphql';
import { AuthService } from '../../services/auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { debounce, interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-login-page',
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPage implements OnInit, OnDestroy {
  showMailLogin: boolean;
  showSSO: boolean;
  loginUrl: string;
  private subscriptions: Subscription[] = [];
  showNoSignupBanner: boolean;
  showUserPasswordLogin: boolean;
  errorMessage: string;
  loading: boolean = true;

  constructor(
    protected readonly serverConfig: ServerConfigService,
    private readonly activatedRoute: ActivatedRoute,
    private readonly router: Router,
    private readonly changeRef: ChangeDetectorRef,
    private readonly authService: AuthService,
  ) {
    this.loginUrl = serverConfig.apiUrl + '/oauth2/authorization/';
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async ngOnInit() {
    if (this.serverConfig.hasProfile(GqlProfileName.SelfHosted)) {
      this.showNoSignupBanner = false;
      this.showUserPasswordLogin = true;
    } else {
      this.showNoSignupBanner = true;
    }

    // const redirectUrl = this.activatedRoute.snapshot.queryParams.redirectUrl;
    // if (redirectUrl) {
    //   this.authService.rememberRedirectUrl(redirectUrl)
    // }

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
            this.showSSO = this.serverConfig.hasProfile(GqlProfileName.AuthSso);
            this.showMailLogin = this.serverConfig.hasProfile(
              GqlProfileName.AuthMail,
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
    } catch (e) {
      this.errorMessage = e.message;
      this.changeRef.detectChanges();
    }
  }
}
