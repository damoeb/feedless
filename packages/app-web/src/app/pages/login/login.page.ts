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
  // showUserPasswordLogin: boolean;
  showMailLogin: boolean;
  showSSO: boolean;
  loginUrl: string;
  private subscriptions: Subscription[] = [];
  canLogin: boolean;
  canCreateUser: boolean;
  hasWaitList: boolean;
  showLogin: boolean;

  constructor(
    private readonly serverSettings: ServerSettingsService,
    private readonly router: Router,
    private readonly authService: AuthService,
    private readonly authSettings: AuthService,
  ) {
    this.loginUrl = serverSettings.apiUrl + '/oauth2/authorization/';
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async ngOnInit() {
    this.canLogin = this.serverSettings.isEnabled(GqlFeatureName.CanLogin);
    this.canCreateUser = this.serverSettings.isEnabled(
      GqlFeatureName.CanCreateUser,
    );
    this.hasWaitList = this.serverSettings.isEnabled(
      GqlFeatureName.HasWaitList,
    );
    console.log('this.canLogin', this.canLogin);
    console.log('this.canCreateUser', this.canCreateUser);
    console.log('this.hasWaitList', this.hasWaitList);
    this.subscriptions.push(
      this.authSettings.isAuthenticated().subscribe(async (authenticated) => {
        if (authenticated) {
          await this.router.navigateByUrl('/');
        } else {
          this.showSSO = this.serverSettings.isEnabled(GqlFeatureName.AuthSso);
          console.log('this.showSSO', this.showSSO);
          this.showMailLogin = this.serverSettings.isEnabled(
            GqlFeatureName.AuthMail,
          );
          console.log('this.showMailLogin', this.showMailLogin);
        }
      }),
    );
  }
}
