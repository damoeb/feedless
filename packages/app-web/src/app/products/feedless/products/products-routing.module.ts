import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ProductsPage } from './products.page';

const routes: Routes = [
  {
    path: ':productId',
    component: ProductsPage,
  },
  {
    path: ':productId/buy',
    loadChildren: () =>
      import('../../../pages/pricing/pricing.module').then(
        (m) => m.PricingPageModule,
      ),
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ProductsPageRoutingModule {}
