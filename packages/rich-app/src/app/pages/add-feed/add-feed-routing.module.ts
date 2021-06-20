import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { AddFeedPage } from './add-feed.page';

const routes: Routes = [
  {
    path: '',
    component: AddFeedPage
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AddFeedPageRoutingModule {}
