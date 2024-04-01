import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { BuyPage } from './buy.page';

const routes: Routes = [
  {
    path: '',
    component: BuyPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ProductsPageRoutingModule {}
