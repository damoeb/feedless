import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { LinkCliPage } from './link-cli.page';

const routes: Routes = [
  {
    path: '',
    component: LinkCliPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class LinkCliRoutingModule {}
