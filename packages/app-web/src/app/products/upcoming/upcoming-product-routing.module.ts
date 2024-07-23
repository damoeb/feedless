import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UpcomingProductPage } from './upcoming-product-page.component';

const routes: Routes = [
  // {
  //   path: ':url',
  //   component: UpcomingProductPage,
  // },
  {
    path: 'events/:state',
    component: UpcomingProductPage,
  },
  {
    path: 'events/:state/:country/:place',
    component: UpcomingProductPage,
  },
  {
    path: 'events/:state/:country/:place/:year/:month',
    component: UpcomingProductPage,
  },
  {
    // H/Zurich/Affoltern%20a.A./2024/6/3
    path: 'events/:state/:country/:place/:year/:month/:day',
    component: UpcomingProductPage,
  },
  {
    path: '**',
    redirectTo: '/events/ch',
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class UpcomingProductRoutingModule {}
