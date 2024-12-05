import { Routes } from '@angular/router';
import { CheckoutPage } from './checkout.page';

export const CHECKOUT_ROUTES: Routes = [
  {
    path: '',
    component: CheckoutPage,
  },
  {
    path: ':productId',
    component: CheckoutPage,
  },
];
