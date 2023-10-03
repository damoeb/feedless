import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { VisualDiffPage } from './visual-diff.page';

const routes: Routes = [
  {
    path: '',
    component: VisualDiffPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class VisualDiffPageRoutingModule {}
