import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'search',
    pathMatch: 'full',
  },
  // {
  //   path: 'folder/:id',
  //   loadChildren: () =>
  //     import('./folder/folder.module').then((m) => m.FolderPageModule),
  // },
  {
    path: 'search',
    loadChildren: () =>
      import('./components/search/search.module').then(
        (m) => m.SearchPageModule
      ),
  },
  {
    path: 'bucket',
    loadChildren: () =>
      import('./components/bucket/bucket.module').then(
        (m) => m.BucketPageModule
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
