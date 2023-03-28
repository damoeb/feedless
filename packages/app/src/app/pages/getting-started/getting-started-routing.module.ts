import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { GettingStartedPage } from './getting-started.page';

const routes: Routes = [
  {
    path: '',
    component: GettingStartedPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class GettingStartedPageRoutingModule {}
