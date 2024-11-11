import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ChangeTrackerProductPage } from './change-tracker-product.page';
import { DefaultRoutes } from '../default-routes';

const routes: Routes = [
  {
    path: '',
    component: ChangeTrackerProductPage,
    children: [
      {
        path: '',
        loadChildren: () =>
          import('./about/about-pc-tracker.module').then(
            (m) => m.AboutPcTrackerModule,
          ),
      },
      {
        path: 'trackers/:trackerId',
        loadChildren: () =>
          import('./tracker-details/tracker-details.module').then(
            (m) => m.TrackerDetailsPageModule,
          ),
      },
      {
        path: 'license',
        loadChildren: () =>
          import('../../pages/license/license.module').then(
            (m) => m.LicensePageModule,
          ),
      },
      ...DefaultRoutes,
    ],
  },
  {
    path: '**',
    redirectTo: '/',
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class PageChangeTrackerPageRoutingModule {}
