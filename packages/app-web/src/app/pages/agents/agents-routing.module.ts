import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { AgentsPage } from './agents.page';

const routes: Routes = [
  {
    path: '',
    component: AgentsPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AgentsRoutingModule {}
