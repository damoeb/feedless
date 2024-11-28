import { Routes } from '@angular/router';

export const PAYMENT_ROUTES: Routes = [
  {
    path: ':billingId',
    loadComponent: () => import('./payment.page').then((m) => m.PaymentPage),
  },
];
