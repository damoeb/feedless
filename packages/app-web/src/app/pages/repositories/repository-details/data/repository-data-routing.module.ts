import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { RepositoryDataPage } from './repository-data.page';

const routes: Routes = [
  {
    path: '',
    component: RepositoryDataPage
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RepositoryDataPageRoutingModule {
}
