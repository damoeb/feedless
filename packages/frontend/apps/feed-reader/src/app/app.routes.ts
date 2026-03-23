import { Route } from '@angular/router';

const loadCreateAuctionAlertPage = () =>
  import('../pages/feed-subscriptions/feed-subscriptions.page').then(
    (m) => m.FeedSubscriptionsPage,
  );

export const appRoutes: Route[] = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: loadCreateAuctionAlertPage,
  },
  // {
  //   path: '**',
  //   loadComponent: loadCreateAuctionAlertPage,
  // },
];
