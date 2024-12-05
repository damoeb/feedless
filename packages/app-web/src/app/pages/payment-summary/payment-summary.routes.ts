import { Routes } from '@angular/router';
import { PaymentSummaryPage } from './payment-summary.page';

export const PAYMENT_SUMMARY_ROUTES: Routes = [
  {
    path: ':billingId',
    component: PaymentSummaryPage,
  },
];
