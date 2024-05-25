import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { SecretsPage } from './secrets.page';

const routes: Routes = [
  {
    path: '',
    component: SecretsPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class SecretsPageRoutingModule {}
