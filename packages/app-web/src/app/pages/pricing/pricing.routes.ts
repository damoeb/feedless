import { Routes } from '@angular/router';

export const PRICING_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pricing.page').then((m) => m.PricingPage),
  },
  {
    path: ':productId',
    loadComponent: () => import('./pricing.page').then((m) => m.PricingPage),
  },
];
