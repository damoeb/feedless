import { Routes } from '@angular/router';

export const READER_ROUTES: Routes = [
  {
    path: ':url',
    loadComponent: () =>
      import('./reader-product.page').then((m) => m.ReaderProductPage),
  },
  {
    path: '',
    loadComponent: () =>
      import('./reader-product.page').then((m) => m.ReaderProductPage),
  },
  {
    path: '**',
    redirectTo: '/',
  },
  {
    path: '',
    outlet: 'sidemenu',
    loadComponent: () =>
      import('./reader-menu/reader-menu.component').then(
        (m) => m.ReaderMenuComponent,
      ),
  },
];
