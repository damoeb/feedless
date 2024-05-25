import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { ServerSettingsService } from '../services/server-settings.service';
import { CanActivate, UrlTree } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class SelfHostingGuardService implements CanActivate {
  constructor(private readonly serverSettings: ServerSettingsService) {}

  canActivate(): Observable<boolean | UrlTree> {
    return of(this.serverSettings.isSelfHosted());
  }
}
