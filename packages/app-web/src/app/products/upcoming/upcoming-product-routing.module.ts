import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UpcomingProductPage } from './upcoming-product-page.component';

const routes: Routes = [
  {
    path: 'events/in',
    component: UpcomingProductPage,
  },
  {
    path: 'events/in/:state/:country/:place/within/:perimeter/on/:year/:month/:day',
    data: {
      lang: 'en'
    },
    component: UpcomingProductPage,
  },
  {
    path: 'events/in/:state/:country/:place/innerhalb/:perimeter/am/:year/:month/:day',
    data: {
      lang: 'de'
    },
    component: UpcomingProductPage,
  },
  {
    path: '**',
    redirectTo: 'events/in',
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class UpcomingProductRoutingModule {}
