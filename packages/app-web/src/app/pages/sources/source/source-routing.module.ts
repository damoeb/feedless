import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { SourcePage } from './source.page';

const routes: Routes = [
  {
    path: '',
    component: SourcePage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class SourcePageRoutingModule {}
