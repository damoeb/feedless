import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { BucketsPage } from './buckets.page';

const routes: Routes = [
  {
    path: '',
    component: BucketsPage
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class BucketsPageRoutingModule {}
