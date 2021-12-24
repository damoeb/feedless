import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { InspectPage } from './inspect.page';

const routes: Routes = [
  {
    path: '',
    component: InspectPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class InspectPageRoutingModule {}
