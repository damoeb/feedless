import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { RepositorySourcesPage } from './repository-sources.page';

const routes: Routes = [
  {
    path: '',
    component: RepositorySourcesPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class RepositorySourcesPageRoutingModule {}
