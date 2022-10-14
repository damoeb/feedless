import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { BucketEditPage } from './bucket-edit.page';

const routes: Routes = [
  {
    path: '',
    component: BucketEditPage
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class BucketEditPageRoutingModule {}
