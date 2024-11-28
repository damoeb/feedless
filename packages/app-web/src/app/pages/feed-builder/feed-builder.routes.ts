import { Routes } from '@angular/router';

export const FEED_BUILDER_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./feed-builder.page').then((m) => m.FeedBuilderPage),
  },
];
