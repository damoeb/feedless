import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { ArticlePage } from './article.page';

const routes: Routes = [
  {
    path: '',
    component: ArticlePage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class BucketPageRoutingModule {}
