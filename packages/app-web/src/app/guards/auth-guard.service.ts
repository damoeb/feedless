import { Injectable } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthGuardService {
  constructor(private readonly authService: AuthService) {
  }

  canActivate(): Observable<boolean> {
    // await this.authService.requireAnyAuthToken();
    // if (await this.authService.isAuthenticated()) {
    //   return true;
    // }
    // return this.router.createUrlTree(['/login']);
    return this.authService.isAuthenticated();
  }
}
