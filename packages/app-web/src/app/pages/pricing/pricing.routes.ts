import { Routes } from '@angular/router';
import { PricingPage } from './pricing.page';

export const PRICING_ROUTES: Routes = [
  {
    path: '',
    component: PricingPage,
  },
  {
    path: ':productId',
    component: PricingPage,
  },
];
