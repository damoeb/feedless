import { Routes } from '@angular/router';

export const DIRECTORY_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./directory.page').then((m) => m.DirectoryPage),
  },
];
