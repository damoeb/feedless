import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FeedTilesPage } from './feed-tiles.page';

const routes: Routes = [
  {
    path: '',
    component: FeedTilesPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class FeedTilesRoutingModule {}
