import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UpcomingProductPage } from './upcoming-product-page.component';

const routes: Routes = [
  {
    path: 'events/near',
    component: UpcomingProductPage,
  },
  {
    // H/Zurich/Affoltern%20a.A./2024/6/3
    path: 'events/near/:state/:country/:place/within/:perimeter/on/:year/:month/:day',
    component: UpcomingProductPage,
  },
  {
    path: 'events/bei/:state/:country/:place/innerhalb/:perimeter/am/:year/:month/:day',
    component: UpcomingProductPage,
  },
  {
    path: '**',
    redirectTo: 'events/near',
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class UpcomingProductRoutingModule {}
