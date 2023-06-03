import { Component, OnDestroy, OnInit } from '@angular/core';
import { ServerSettingsService } from '../../services/server-settings.service';
import { GqlFeatureName } from '../../../generated/graphql';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-login',
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.scss'],
})
export class LoginPage implements OnInit, OnDestroy {
  showUserPasswordLogin: boolean;
  showMailLogin: boolean;
  showSSO: boolean;
  loginUrl: string;
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly serverSettings: ServerSettingsService,
    private readonly router: Router,
    private readonly authService: AuthService,
    private readonly authSettings: AuthService
  ) {
    this.loginUrl = serverSettings.apiUrl + '/oauth2/authorization/';
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async ngOnInit() {
    this.subscriptions.push(
      this.authSettings.isAuthenticated().subscribe(async (authenticated) => {
        if (authenticated) {
          await this.router.navigateByUrl('/');
        } else {
          this.showSSO = !this.serverSettings.isFeatureOff(
            GqlFeatureName.AuthSso
          );
          this.showUserPasswordLogin = !this.serverSettings.isFeatureOff(
            GqlFeatureName.AuthRoot
          );
          this.showMailLogin = !this.serverSettings.isFeatureOff(
            GqlFeatureName.AuthMail
          );
        }
      })
    );
  }

  async loginRoot(email: string | number, password: string | number) {
    await this.authService.requestAuthForRoot({
      email: `${email}`,
      secretKey: `${password}`,
    });
    await this.router.navigateByUrl('/');
  }
}
