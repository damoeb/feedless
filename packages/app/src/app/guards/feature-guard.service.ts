import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { ServerSettingsService } from '../services/server-settings.service';
import { GqlFeatureName } from '../../generated/graphql';

@Injectable({
  providedIn: 'root'
})
export class FeatureGuardService implements CanActivate {
  constructor(private readonly router: Router,
              private readonly serverSettings: ServerSettingsService) {
  }

  canActivate(): boolean | UrlTree {
    if (!this.serverSettings.hasFeature(GqlFeatureName.Database)) {
      return this.router.parseUrl('/wizard');
    }
    return true;
  }
}
