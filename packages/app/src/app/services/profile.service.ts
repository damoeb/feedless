import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class ProfileService {
  constructor() {}

  getEmail(): string {
    return 'karl@may.ch';
  }

  isAdvancedUser(): boolean {
    return true;
  }
}
