import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { WorkflowBuilderPage } from './workflow-builder.page';

const routes: Routes = [
  {
    path: '',
    component: WorkflowBuilderPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class WorkflowBuilderPageRoutingModule {}
