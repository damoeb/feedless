import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './auth/auth.guard';

const routes: Routes = [
  {
    path: '',
    // canActivate: [AuthGuard],
    redirectTo: 'inspect',
    pathMatch: 'full',
  },
  {
    path: 'folder/:id',
    canActivate: [AuthGuard],
    loadChildren: () =>
      import('./pages/folder/folder.module').then((m) => m.FolderPageModule),
  },
  {
    path: 'write',
    canActivate: [AuthGuard],
    loadChildren: () =>
      import('./pages/write/write.module').then((m) => m.WritePageModule),
  },
  {
    path: 'bucket',
    canActivate: [AuthGuard],
    loadChildren: () =>
      import('./pages/bucket/bucket.module').then((m) => m.BucketPageModule),
  },
  {
    path: 'read',
    canActivate: [AuthGuard],
    loadChildren: () =>
      import('./pages/reader/reader.module').then((m) => m.ReaderPageModule),
  },
  {
    path: 'integrate',
    canActivate: [AuthGuard],
    loadChildren: () =>
      import('./pages/integrate/integrate.module').then(
        (m) => m.IntegratePageModule
      ),
  },
  {
    path: 'login',
    loadChildren: () =>
      import('./pages/login/login.module').then((m) => m.LoginPageModule),
  },
  {
    path: 'buckets',
    loadChildren: () =>
      import('./pages/buckets/buckets.module').then((m) => m.BucketsPageModule),
  },
  {
    path: 'inspect',
    loadChildren: () =>
      import('./pages/inspect/inspect.module').then((m) => m.InspectPageModule),
  },
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, { preloadingStrategy: PreloadAllModules }),
  ],
  exports: [RouterModule],
})
export class AppRoutingModule {}
