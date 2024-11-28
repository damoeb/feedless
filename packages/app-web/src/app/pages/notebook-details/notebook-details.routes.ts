import { Routes } from '@angular/router';

export const NOTEBOOK_DETAILS_ROUTES: Routes = [
  {
    path: ':notebookId',
    loadComponent: () =>
      import('./notebook-details.page').then((m) => m.NotebookDetailsPage),
  },
  {
    path: ':notebookId/:noteId',
    loadComponent: () =>
      import('./notebook-details.page').then((m) => m.NotebookDetailsPage),
  },
];
