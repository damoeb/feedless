import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { RssBuilderPage } from './rss-builder.page';

const routes: Routes = [
  {
    path: ':url',
    component: RssBuilderPage,
  },
  {
    path: '',
    component: RssBuilderPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class RssBuilderPageRoutingModule {}
