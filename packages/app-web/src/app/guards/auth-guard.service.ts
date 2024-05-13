import { Injectable } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Observable, of, switchMap } from 'rxjs';
import { ServerSettingsService } from '../services/server-settings.service';
import { CanActivate, Router, UrlTree } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class AuthGuardService implements CanActivate {
  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly serverSettings: ServerSettingsService,
  ) {}

  canActivate(): Observable<boolean | UrlTree> {
    console.log('isSelfHosted', this.serverSettings.isSelfHosted());
    if (this.serverSettings.isSelfHosted()) {
      return this.authService.authorizationChange().pipe(
        switchMap((authentication) => {
          console.log('authenticated', authentication?.loggedIn);
          if (authentication?.loggedIn) {
            return of(true);
          } else {
            return of(
              this.router.createUrlTree(['/login'], {
                queryParams: { redirectUrl: location.pathname },
                queryParamsHandling: 'merge',
              }),
            );
          }
        }),
      );
    } else {
      return of(false);
    }
  }
}
