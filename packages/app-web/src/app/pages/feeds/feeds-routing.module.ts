import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { FeedsPage } from './feeds.page';

const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    component: FeedsPage,
  },
  {
    path: ':id',
    loadChildren: () =>
      import('./feed/feed.module').then((m) => m.FeedPageModule),
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class FeedsPageRoutingModule {}
