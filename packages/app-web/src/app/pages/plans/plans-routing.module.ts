import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { PlansPage } from './plans.page';

const routes: Routes = [
  {
    path: '',
    component: PlansPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class PlansPageRoutingModule {}
