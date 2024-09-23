import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UntoldNotesProductPage } from './untold-notes-product.page';
import { DefaultRoutes } from '../default-routes';

const routes: Routes = [
  {
    path: '',
    component: UntoldNotesProductPage,
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
