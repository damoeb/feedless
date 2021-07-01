import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { AddBucketPage } from './add-bucket.page';

const routes: Routes = [
  {
    path: '',
    component: AddBucketPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AddBucketPageRoutingModule {}
