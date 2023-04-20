import { Injectable } from '@angular/core';
import { CanActivate, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root',
})
export class AuthGuardService implements CanActivate {
  constructor(private readonly authService: AuthService) {}

  async canActivate(): Promise<boolean | UrlTree> {
    // await this.authService.requireAnyAuthToken();
    // if (await this.authService.isAuthenticated()) {
    //   return true;
    // }
    // return this.router.createUrlTree(['/login']);
    return this.authService.isAuthenticated();
  }
}
