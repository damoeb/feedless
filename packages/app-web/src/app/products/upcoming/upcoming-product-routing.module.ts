import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UpcomingProductPage } from './upcoming-product-page.component';

const routes: Routes = [
  {
    path: ':url',
    component: UpcomingProductPage
  },
  {
    path: '',
    component: UpcomingProductPage
  },
  {
    path: '**',
    redirectTo: '/'
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UpcomingProductRoutingModule {
}
