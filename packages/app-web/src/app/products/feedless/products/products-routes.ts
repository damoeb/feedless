import { Routes } from '@angular/router';
import { ProductsPage } from './products.page';

export const PRODUCT_ROUTES: Routes = [
  {
    path: ':productId',
    component: ProductsPage,
  },
  {
    path: ':productId/buy',
    loadChildren: () =>
      import('../../../pages/pricing/pricing.routes').then(
        (m) => m.PRICING_ROUTES,
      ),
  },
];
