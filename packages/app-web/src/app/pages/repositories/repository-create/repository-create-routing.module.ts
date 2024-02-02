import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { RepositoryCreatePage } from './repository-create.page';

const routes: Routes = [
  {
    path: '',
    component: RepositoryCreatePage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class RepositoryCreatePageRoutingModule {}
