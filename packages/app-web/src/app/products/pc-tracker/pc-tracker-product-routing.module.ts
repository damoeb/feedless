import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { PcTrackerProductPage } from './pc-tracker-product.page';
import { ProductService } from '../../services/product.service';
import { AuthGuardService } from '../../guards/auth-guard.service';

const routes: Routes = [
  {
    path: '',
    component: PcTrackerProductPage,
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
    ],
  },
  ...ProductService.defaultRoutes,
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
