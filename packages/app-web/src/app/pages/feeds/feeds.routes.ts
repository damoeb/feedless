import { Routes } from '@angular/router';

export const FEED_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./feeds.page').then((m) => m.FeedsPage),
  },
];
