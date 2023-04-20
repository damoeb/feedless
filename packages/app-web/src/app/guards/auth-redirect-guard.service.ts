import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root',
})
export class AuthRedirectGuardService implements CanActivate {
  constructor(
    private readonly router: Router,
    private readonly authService: AuthService
  ) {}

  async canActivate(): Promise<boolean | UrlTree> {
    const authenticated = await this.authService.isAuthenticated();
    if (!authenticated) {
      localStorage.setItem('requestedUrl', location.href);
      await this.router.createUrlTree(['/login']);
    }
    return authenticated;
  }
}
