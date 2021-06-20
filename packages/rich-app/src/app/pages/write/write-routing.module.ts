import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { WritePage } from './write.page';

const routes: Routes = [
  {
    path: '',
    component: WritePage
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class WritePageRoutingModule {}
