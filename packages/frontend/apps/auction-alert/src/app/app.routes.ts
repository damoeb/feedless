import { Route } from '@angular/router';

const loadCreateAuctionAlertPage = () =>
  import('./pages/create-auction-alert/create-auction-alert.page').then(
    (m) => m.CreateAuctionAlertPage,
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
