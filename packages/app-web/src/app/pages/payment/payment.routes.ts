import { Routes } from '@angular/router';
import { PaymentPage } from './payment.page';

export const PAYMENT_ROUTES: Routes = [
  {
    path: ':billingId',
    component: PaymentPage,
  },
];
