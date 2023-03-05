import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { BucketsPage } from './buckets.page';

const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'buckets',
  },
  {
    path: 'buckets',
    component: BucketsPage,
  },
  {
    path: 'buckets/:id',
    loadChildren: () =>
      import('./bucket/bucket.module').then((m) => m.BucketPageModule),
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class BucketsPageRoutingModule {}
