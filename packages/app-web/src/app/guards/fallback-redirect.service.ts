import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root',
})
export class FallbackRedirectService implements CanActivate {
  constructor(
    private readonly router: Router,
    private readonly authService: AuthService
  ) {}

  async canActivate(): Promise<UrlTree | boolean> {
    if (await this.authService.isAuthenticated()) {
      return this.router.createUrlTree(['/buckets']);
    }
    return this.router.createUrlTree(['/getting-started']);
  }
}
