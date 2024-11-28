import { Routes } from '@angular/router';

export const LOGIN_ROUTES: Routes = [
  {
    path: '**',
    loadComponent: () => import('./login.page').then((m) => m.LoginPage),
  },
];
