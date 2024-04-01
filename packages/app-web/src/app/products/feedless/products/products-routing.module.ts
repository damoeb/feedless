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
      import('../buy/buy.module').then((m) => m.ProductsPageModule),
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ProductsPageRoutingModule {}
