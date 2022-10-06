import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private JWT_TOKEN = 'JWT_TOKEN';

  constructor() {}

  public isAuthenticated(): boolean {
    // const token = get(this.JWT_TOKEN);
    //
    // if (isUndefined(token)) {
    //   console.log('not authenticated - no token found');
    //   return false;
    // }

    return true;
  }

  handleAuthToken(token: string) {
    // set(this.JWT_TOKEN, token, { sameSite: 'strict' });
    console.log('Updated auth token');
  }
}
