import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { PaymentPage } from './payment.page';

const routes: Routes = [
  {
    path: ':billingId',
    component: PaymentPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class PaymentPageRoutingModule {}
