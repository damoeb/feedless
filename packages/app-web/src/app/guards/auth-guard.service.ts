import { inject, Injectable } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { firstValueFrom, Observable, of, switchMap } from 'rxjs';
import { CanActivate, Router, UrlTree } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class AuthGuardService implements CanActivate {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

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

  async assertLoggedIn() {
    const loggedIn = await firstValueFrom(this.canActivate());
    if (loggedIn !== true) {
      await this.router.navigateByUrl(loggedIn as UrlTree);
    }
  }
}
