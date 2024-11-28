import { Routes } from '@angular/router';

export const PRODUCT_ROUTES: Routes = [
  {
    path: ':productId',
    loadComponent: () => import('./products.page').then((m) => m.ProductsPage),
  },
  {
    path: ':productId/buy',
    loadChildren: () =>
      import('../../../pages/pricing/pricing.routes').then(
        (m) => m.PRICING_ROUTES,
      ),
  },
];
