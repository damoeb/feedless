import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ArticlesPage } from './articles.page';

const routes: Routes = [
  {
    path: '',
    component: ArticlesPage,
  },
  {
    path: ':id',
    loadChildren: () =>
      import('./article/article.module').then((m) => m.ArticlePageModule),
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ArticlesPageRoutingModule {}
