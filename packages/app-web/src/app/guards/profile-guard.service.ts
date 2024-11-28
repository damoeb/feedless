import { inject, Injectable } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Observable, of, switchMap } from 'rxjs';
import { CanActivate, UrlTree } from '@angular/router';
import { SessionService } from '../services/session.service';

@Injectable({
  providedIn: 'root',
})
export class ProfileGuardService implements CanActivate {
  private readonly authService = inject(AuthService);
  private readonly sessionService = inject(SessionService);

  canActivate(): Observable<boolean | UrlTree> {
    return this.authService.authorizationChange().pipe(
      switchMap((authentication) => {
        if (authentication?.loggedIn) {
          this.sessionService.finalizeProfile();
        }
        return of(true);
      }),
    );
  }
}
