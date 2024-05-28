import { Injectable } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Observable, of, switchMap } from 'rxjs';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { SessionService } from '../services/session.service';

@Injectable({
  providedIn: 'root',
})
export class AuthGuardService implements CanActivate {
  constructor(
    private readonly authService: AuthService,
    private readonly sessionService: SessionService,
    private readonly router: Router,
  ) {}

  canActivate(): Observable<boolean | UrlTree> {
    return this.authService.authorizationChange().pipe(
      switchMap((authentication) => {
        if (authentication?.loggedIn) {
          this.sessionService.finalizeProfile();
          return of(true);
        } else {
          console.log('redirect to login');
          return of(
            this.router.createUrlTree(['/login'], {
              queryParams: { redirectUrl: location.pathname },
              queryParamsHandling: 'merge',
            }),
          );
        }
      }),
    );
  }
}
