import { Component, OnInit } from '@angular/core';
import { ServerSettingsService } from '../../services/server-settings.service';
import { GqlFeatureName } from '../../../generated/graphql';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.scss'],
})
export class LoginPage implements OnInit {
  showRootLogin: boolean;
  showMailLogin: boolean;
  showSSO: boolean;
  loginUrl: string;

  constructor(
    private readonly serverSettings: ServerSettingsService,
    private readonly router: Router,
    private readonly authSettings: AuthService
  ) {
    this.loginUrl = serverSettings.apiUrl + '/oauth2/authorization/';
  }

  async ngOnInit() {
    if (await this.authSettings.isAuthenticated()) {
      await this.router.navigateByUrl('/');
    } else {
      this.showSSO = this.serverSettings.canUseFeature(GqlFeatureName.AuthSso);
      this.showRootLogin = this.serverSettings.canUseFeature(
        GqlFeatureName.AuthAllowRoot
      );
      this.showMailLogin = this.serverSettings.canUseFeature(
        GqlFeatureName.AuthMail
      );
    }
  }
}
