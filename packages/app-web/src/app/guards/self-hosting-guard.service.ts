import { inject, Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { ServerConfigService } from '../services/server-config.service';
import { CanActivate, UrlTree } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class SelfHostingGuardService implements CanActivate {
  private readonly serverConfig = inject(ServerConfigService);

  canActivate(): Observable<boolean | UrlTree> {
    return of(this.serverConfig.isSelfHosted());
  }
}
