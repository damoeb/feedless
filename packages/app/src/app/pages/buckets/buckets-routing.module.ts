import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { BucketsPage } from './buckets.page';

const routes: Routes = [
  {
    path: '',
    component: BucketsPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class SearchPageRoutingModule {}
