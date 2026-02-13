import { inject, Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { CanActivate, UrlTree } from '@angular/router';
import { ServerConfigService } from '../services/server-config.service';

@Injectable({
  providedIn: 'root',
})
export class SaasGuardService implements CanActivate {
  private readonly serverConfig = inject(ServerConfigService);

  canActivate(): Observable<boolean | UrlTree> {
    return of(!this.serverConfig.isSelfHosted());
  }
}
