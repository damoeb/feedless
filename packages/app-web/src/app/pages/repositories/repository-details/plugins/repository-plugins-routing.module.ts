import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { RepositoryPluginsPage } from './repository-plugins.page';

const routes: Routes = [
  {
    path: '',
    component: RepositoryPluginsPage
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RepositoryPluginsPageRoutingModule {
}
