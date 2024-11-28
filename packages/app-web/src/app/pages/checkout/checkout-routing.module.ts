import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';



const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./checkout.page').then(m => m.CheckoutPage),
  },
  {
    path: ':productId',
    loadComponent: () => import('./checkout.page').then(m => m.CheckoutPage),
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class CheckoutPageRoutingModule {}
