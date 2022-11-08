import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SettingsService {

  private corrId = 'A2F4';

  constructor() { }

  getCorrId(): string {
    return this.corrId;
  }
}
