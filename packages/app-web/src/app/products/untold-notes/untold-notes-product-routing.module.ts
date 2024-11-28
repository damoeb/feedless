import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';


import { DefaultRoutes } from '../default-routes';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./untold-notes-product.page').then(m => m.UntoldNotesProductPage),
    children: [
      {
        path: '',
        loadChildren: () =>
          import('./about/about-untold-notes.module').then(
            (m) => m.AboutUntoldNotesModule,
          ),
      },
    ],
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
export class UntoldNotesPageRoutingModule {}
