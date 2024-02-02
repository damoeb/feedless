import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ReaderProductPage } from './reader-product.page';

const routes: Routes = [
  {
    path: ':url',
    component: ReaderProductPage,
  },
  {
    path: '',
    component: ReaderProductPage,
  },
  {
    path: '**',
    redirectTo: '/',
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ReaderProductRoutingModule {}
