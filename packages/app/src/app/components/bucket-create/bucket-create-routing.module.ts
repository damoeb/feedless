import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { BucketCreatePage } from './bucket-create.page';

const routes: Routes = [
  {
    path: '',
    component: BucketCreatePage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class BucketCreatePageRoutingModule {}
