import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { RepositorySettingsPage } from './repository-settings-page.component';

const routes: Routes = [
  {
    path: '',
    component: RepositorySettingsPage
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RepositorySettingsPageRoutingModule {
}
