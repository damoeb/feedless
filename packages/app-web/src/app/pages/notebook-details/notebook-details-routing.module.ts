import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';


const routes: Routes = [
  {
    path: ':notebookId',
    loadComponent: () => import('./notebook-details.page').then(m => m.NotebookDetailsPage),
  },
  {
    path: ':notebookId/:noteId',
    loadComponent: () => import('./notebook-details.page').then(m => m.NotebookDetailsPage),
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class NotebookDetailsRoutingModule {}
