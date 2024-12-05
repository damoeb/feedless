import { Routes } from '@angular/router';
import { LoginPage } from './login.page';

export const LOGIN_ROUTES: Routes = [
  {
    path: '**',
    component: LoginPage,
  },
];
