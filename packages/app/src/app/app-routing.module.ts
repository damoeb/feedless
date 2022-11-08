import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'search',
    pathMatch: 'full',
  },
  {
    path: 'search',
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
    path: 'article/:id',
    loadChildren: () =>
      import('./pages/article-page/article.module').then(
        (m) => m.ArticlePageModule
      ),
  },
  // {
  //   path: 'importer/:id',
  //   loadChildren: () =>
  //     import('./pages/importer-page/importer.module').then(
  //       (m) => m.ImporterPageModule
  //     ),
  // },
  {
    path: 'bucket/new',
    loadChildren: () => import('./components/bucket-create/bucket-create.module').then( m => m.BucketCreatePageModule)
  },
  {
    path: 'bucket/:id/feed/:feedId',
    loadChildren: () => import('./components/importer-edit/importer-edit.module').then( m => m.ImporterEditPageModule)
  },
  {
    path: 'bucket/:id/edit',
    loadChildren: () => import('./components/bucket-edit/bucket-edit.module').then( m => m.BucketEditPageModule)
  },
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, { preloadingStrategy: PreloadAllModules }),
  ],
  exports: [RouterModule],
})
export class AppRoutingModule {}
