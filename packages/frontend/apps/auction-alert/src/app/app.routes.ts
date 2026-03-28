import { Route } from '@angular/router';

const loadCreateAuctionAlertPage = () =>
  import('./pages/create-auction-alert/create-auction-alert.page').then(
    (m) => m.CreateAuctionAlertPage,
  );

const loadCheckoutSuccessPage = () =>
  import('./pages/checkout-success/checkout-success.page').then(
    (m) => m.CheckoutSuccessPage,
  );

const loadCheckoutCancelPage = () =>
  import('./pages/checkout-cancel/checkout-cancel.page').then(
    (m) => m.CheckoutCancelPage,
  );

export const appRoutes: Route[] = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: loadCreateAuctionAlertPage,
  },
  {
    path: 'checkout/erfolg',
    loadComponent: loadCheckoutSuccessPage,
  },
  {
    path: 'checkout/abbruch',
    loadComponent: loadCheckoutCancelPage,
  },
  // {
  //   path: '**',
  //   loadComponent: loadCreateAuctionAlertPage,
  // },
];
