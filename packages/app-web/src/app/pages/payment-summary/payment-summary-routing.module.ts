import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';



const routes: Routes = [
  {
    path: ':billingId',
    loadComponent: () => import('./payment-summary.page').then(m => m.PaymentSummaryPage),
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class PaymentConfirmationPageRoutingModule {}
