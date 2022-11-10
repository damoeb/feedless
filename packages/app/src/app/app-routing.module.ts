import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadChildren: () =>
      import('./components/search/search.module').then(
        (m) => m.SearchPageModule
      ),
  },
  {
    path: 'bucket/:id',
    loadChildren: () =>
      import('./pages/bucket-page/bucket.module').then(
        (m) => m.BucketPageModule
      ),
  },
  {
    path: 'bucket/new',
    loadChildren: () =>
      import('./components/bucket-create/bucket-create.module').then(
        (m) => m.BucketCreatePageModule
      ),
  },
  {
    path: 'bucket/:id/article/:articleId',
    loadChildren: () =>
      import('./pages/article-page/article.module').then(
        (m) => m.ArticlePageModule
      ),
  },
  {
    path: 'bucket/:id/feeds',
    loadChildren: () =>
      import('./components/bucket-feeds/bucket-feeds.module').then(
        (m) => m.BucketFeedsModule
      ),
  },
  {
    path: 'bucket/:id/feeds/:feedId',
    loadChildren: () =>
      import('./components/importer-edit/importer-edit.module').then(
        (m) => m.ImporterEditPageModule
      ),
  },
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, { preloadingStrategy: PreloadAllModules }),
  ],
  exports: [RouterModule],
})
export class AppRoutingModule {}
