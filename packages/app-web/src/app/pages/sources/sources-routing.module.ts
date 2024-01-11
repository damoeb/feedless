import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { SourcesPage } from './sources.page';

const routes: Routes = [
  {
    path: '',
    component: SourcesPage,
  },
  {
    path: ':id',
    loadChildren: () =>
      import('./source/source.module').then((m) => m.SourcePageModule),
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class SourcesPageRoutingModule {}
