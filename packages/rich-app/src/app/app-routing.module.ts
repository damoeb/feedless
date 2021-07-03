import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'folder/Inbox',
    pathMatch: 'full',
  },
  {
    path: 'folder/:id',
    loadChildren: () =>
      import('./folder/folder.module').then((m) => m.FolderPageModule),
  },
  {
    path: 'write',
    loadChildren: () =>
      import('./pages/write/write.module').then((m) => m.WritePageModule),
  },
  {
    path: 'add-feed',
    loadChildren: () =>
      import('./pages/add-feed/add-feed.module').then(
        (m) => m.AddFeedPageModule
      ),
  },
  {
    path: 'bucket/:id',
    loadChildren: () =>
      import('./pages/bucket/bucket.module').then((m) => m.BucketPageModule),
  },
  {
    path: 'add-bucket',
    loadChildren: () =>
      import('./pages/add-bucket/add-bucket.module').then(
        (m) => m.AddBucketPageModule
      ),
  },
  {
    path: 'item',
    loadChildren: () =>
      import('./pages/item/item.module').then((m) => m.ItemPageModule),
  },
  {
    path: 'reader',
    loadChildren: () =>
      import('./pages/item/item.module').then((m) => m.ItemPageModule),
  },
  {
    path: 'search',
    loadChildren: () =>
      import('./components/search/search.module').then(
        (m) => m.SearchPageModule
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
