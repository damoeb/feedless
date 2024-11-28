import { Routes } from '@angular/router';

export const LICENSE_ROUTES: Routes = [
  {
    path: '**',
    loadComponent: () => import('./license.page').then((m) => m.LicensePage),
  },
];
