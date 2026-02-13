import { inject, Injectable } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { map, Observable } from 'rxjs';
import { CanActivate, UrlTree } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class ProfileGuardService implements CanActivate {
  private readonly authService = inject(AuthService);

  canActivate(): Observable<boolean | UrlTree> {
    return this.authService
      .authorizationChange()
      .pipe(map((authentication) => authentication?.loggedIn));
  }
}
