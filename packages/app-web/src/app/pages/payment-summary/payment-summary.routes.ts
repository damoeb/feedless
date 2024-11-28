import { Routes } from '@angular/router';

export const PAYMENT_SUMMARY_ROUTES: Routes = [
  {
    path: ':billingId',
    loadComponent: () =>
      import('./payment-summary.page').then((m) => m.PaymentSummaryPage),
  },
];
