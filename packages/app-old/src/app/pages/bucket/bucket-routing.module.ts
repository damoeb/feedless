import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { BucketPage } from './bucket.page';

const routes: Routes = [
  {
    path: ':id',
    component: BucketPage,
  },
  {
    path: ':id/:modal',
    component: BucketPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class BucketPageRoutingModule {}
