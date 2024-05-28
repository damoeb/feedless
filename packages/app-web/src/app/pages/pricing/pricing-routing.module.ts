import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { PricingPage } from './pricing.page';

const routes: Routes = [
  {
    path: '',
    component: PricingPage,
  },
  {
    path: ':productId',
    component: PricingPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class PricingPageRoutingModule {}
