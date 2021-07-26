import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { ReadPage } from './read.page';

const routes: Routes = [
  {
    path: ':id',
    component: ReadPage,
  },
  {
    path: '',
    component: ReadPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ItemPageRoutingModule {}
