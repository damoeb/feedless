import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { PaymentSummaryPage } from './payment-summary.page';

const routes: Routes = [
  {
    path: ':billingId',
    component: PaymentSummaryPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class PaymentConfirmationPageRoutingModule {}
