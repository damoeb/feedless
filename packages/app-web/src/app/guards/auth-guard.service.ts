import { Injectable } from '@angular/core';
import { CanActivate, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { map, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthGuardService implements CanActivate {
  constructor(private readonly authService: AuthService) {}

  canActivate(): Observable<boolean> {
    // await this.authService.requireAnyAuthToken();
    // if (await this.authService.isAuthenticated()) {
    //   return true;
    // }
    // return this.router.createUrlTree(['/login']);
    return this.authService.isAuthenticated();
  }
}
