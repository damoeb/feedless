import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ServerSettingsService } from '../../services/server-settings.service';
import { GqlFeatureName, GqlProfileName } from '../../../generated/graphql';
import { AuthService } from '../../services/auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';

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
  canLogin: boolean;
  canSignUp: boolean;
  hasWaitList: boolean;
  showLogin: boolean;
  showNoSignupBanner: boolean;
  showUserPasswordLogin: boolean;
  errorMessage: string;
  loading: boolean = true;

  constructor(
    protected readonly serverSettings: ServerSettingsService,
    private readonly activatedRoute: ActivatedRoute,
    private readonly router: Router,
    private readonly changeRef: ChangeDetectorRef,
    private readonly authService: AuthService,
  ) {
    this.loginUrl = serverSettings.apiUrl + '/oauth2/authorization/';
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async ngOnInit() {
    if (this.serverSettings.hasProfile(GqlProfileName.SelfHosted)) {
      this.showNoSignupBanner = false;
      this.showUserPasswordLogin = true;
    } else {
      this.showNoSignupBanner = true;
      this.canLogin = this.serverSettings.isEnabled(GqlFeatureName.CanLogin);
      this.canSignUp =
        this.serverSettings.isEnabled(GqlFeatureName.CanSignUp) &&
        this.serverSettings.isEnabled(GqlFeatureName.CanCreateUser);
      this.hasWaitList = this.serverSettings.isEnabled(
        GqlFeatureName.HasWaitList,
      );
      this.showLogin = this.canLogin && !this.hasWaitList;
    }

    this.subscriptions.push(
      this.authService.isAuthenticated().subscribe(async (authenticated) => {
        if (authenticated) {
          const { queryParams } = this.activatedRoute.snapshot;
          const { redirectUrl } = queryParams;
          const url = this.router.createUrlTree([redirectUrl || '/'], {});
          await this.router.navigateByUrl(url);
        } else {
          this.showSSO = this.serverSettings.hasProfile(GqlProfileName.AuthSso);
          this.showMailLogin = this.serverSettings.hasProfile(
            GqlProfileName.AuthMail,
          );
        }
        this.loading = false;
        this.changeRef.detectChanges();
      }),
    );
  }

  loginWithUserPassword(email: string | number, password: string | number) {
    this.errorMessage = null;
    return this.authService
      .authorizeUser({
        email: `${email}`,
        secretKey: `${password}`,
      })
      .catch((e) => {
        this.errorMessage = e.message;
        this.changeRef.detectChanges();
      });
  }
}
