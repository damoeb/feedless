import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ImportersPage } from './importers.page';

const routes: Routes = [
  {
    path: '',
    component: ImportersPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ImportersPageRoutingModule {}
