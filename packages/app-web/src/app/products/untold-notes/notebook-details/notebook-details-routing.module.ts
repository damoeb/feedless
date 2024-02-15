import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NotebookDetailsPage } from './notebook-details.page';

const routes: Routes = [
  {
    path: ':notebookId',
    component: NotebookDetailsPage,
  },
  {
    path: ':notebookId/:noteId',
    component: NotebookDetailsPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class NotebookDetailsRoutingModule {}
