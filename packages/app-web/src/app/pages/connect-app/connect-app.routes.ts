import { Routes } from '@angular/router';

export const CONNECT_APP_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./connect-app.page').then((m) => m.ConnectAppPage),
  },
  {
    path: ':link',
    loadComponent: () =>
      import('./connect-app.page').then((m) => m.ConnectAppPage),
  },
];
