import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { FeedDumpProductPage } from './feed-dump-product.page';
import { DefaultRoutes } from '../default-routes';

const routes: Routes = [
  {
    path: '',
    component: FeedDumpProductPage,
  },
  ...DefaultRoutes,
  {
    path: '**',
    redirectTo: '/',
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class FeedDumpProductRoutingModule {}
