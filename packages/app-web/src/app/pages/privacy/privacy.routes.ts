import { Routes } from '@angular/router';

export const PRIVACY_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./privacy.page').then((m) => m.PrivacyPage),
  },
];
