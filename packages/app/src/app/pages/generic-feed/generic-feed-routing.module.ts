import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { GenericFeedPage } from './generic-feed.page';

const routes: Routes = [
  {
    path: '',
    component: GenericFeedPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class GenericFeedPageRoutingModule {}
