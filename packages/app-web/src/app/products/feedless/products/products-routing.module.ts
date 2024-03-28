import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ProductsPage } from './products.page';
import { BuyPage } from '../buy/buy.page';

const routes: Routes = [
  {
    path: ':productId',
    component: ProductsPage,
  },
  {
    path: ':productId/buy',
    component: BuyPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ProductsPageRoutingModule {}
