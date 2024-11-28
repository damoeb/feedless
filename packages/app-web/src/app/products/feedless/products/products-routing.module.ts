import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';



const routes: Routes = [
  {
    path: ':productId',
    loadComponent: () => import('./products.page').then(m => m.ProductsPage),
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
