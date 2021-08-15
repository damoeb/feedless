import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  Router,
  UrlTree,
} from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {
  constructor(private router: Router, private auth: AuthService) {}

  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    console.log('Trying auth-guard');
    if (!this.auth.isAuthenticated()) {
      return this.router.parseUrl('/login');
    }
    return true;
  }
}
