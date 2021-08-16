import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { IntegratePage } from './integrate.page';

const routes: Routes = [
  {
    path: '',
    component: IntegratePage
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class IntegratePageRoutingModule {}
