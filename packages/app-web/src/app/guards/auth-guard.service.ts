import { Injectable } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Observable, of, switchMap } from 'rxjs';
import { CanActivate, Router, UrlTree } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class AuthGuardService implements CanActivate {
  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {}

  canActivate(): Observable<boolean | UrlTree> {
    return this.authService.authorizationChange().pipe(
      switchMap((authentication) => {
        if (authentication?.loggedIn) {
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
