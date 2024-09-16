import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NotebookBuilderPage } from './notebook-builder.page';

const routes: Routes = [
  {
    path: '',
    component: NotebookBuilderPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class NotebookBuilderPageRoutingModule {}
