import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { BucketFeedsPage } from './bucket-feeds.page';

const routes: Routes = [
  {
    path: '',
    component: BucketFeedsPage
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class BucketEditPageRoutingModule {}
