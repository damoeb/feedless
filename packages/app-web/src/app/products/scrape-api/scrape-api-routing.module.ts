import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ScrapeApiPage } from './scrape-api.page';

const routes: Routes = [
  {
    path: '',
    component: ScrapeApiPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ScrapeApiPageRoutingModule {}
