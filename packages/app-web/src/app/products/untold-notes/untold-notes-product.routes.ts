import { Routes } from '@angular/router';

import { DefaultRoutes } from '../default-routes';

export const UNTOLD_NOTES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./untold-notes-product.page').then(
        (m) => m.UntoldNotesProductPage,
      ),
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./about/about-untold-notes.page').then(
            (m) => m.AboutUntoldNotesPage,
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
