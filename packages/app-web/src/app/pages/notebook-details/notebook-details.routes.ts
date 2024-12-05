import { Routes } from '@angular/router';
import { NotebookDetailsPage } from './notebook-details.page';

export const NOTEBOOK_DETAILS_ROUTES: Routes = [
  {
    path: ':notebookId',
    component: NotebookDetailsPage,
  },
  {
    path: ':notebookId/:noteId',
    component: NotebookDetailsPage,
  },
];
