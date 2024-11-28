import { Routes } from '@angular/router';

export const NOTEBOOKS_ROUTING: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./notebooks.page').then((m) => m.NotebooksPage),
  },
];
