import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';



const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pricing.page').then(m => m.PricingPage),
  },
  {
    path: ':productId',
    loadComponent: () => import('./pricing.page').then(m => m.PricingPage),
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class PricingPageRoutingModule {}
