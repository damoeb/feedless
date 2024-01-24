import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { RepositoriesPage } from './repositories.page';

const routes: Routes = [
  {
    path: '',
    component: RepositoriesPage
  },
  {
    path: 'create',
    loadChildren: () =>
      import('./repository-create/repository-create.module').then((m) => m.RepositoryCreatePageModule)
  },
  {
    path: ':repositoryId',
    loadChildren: () =>
      import('./repository-details/repository-details.module').then((m) => m.RepositoryDetailsPageModule)
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RepositoriesPageRoutingModule {
}
