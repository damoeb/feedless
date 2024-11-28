import { Routes } from '@angular/router';

export const FEED_DETAILS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./feed-details.page').then((m) => m.FeedDetailsPage),
  },
];
