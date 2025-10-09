import { Routes } from '@angular/router';

import { DefaultRoutes } from '../default-routes';

export const CHANGE_TRACKER_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./change-tracker-product.page').then((m) => m.ChangeTrackerProductPage),
    children: [
      {
        path: '',
        loadComponent: () => import('./about/about-tracker.page').then((m) => m.AboutTrackerPage),
      },
      {
        path: 'trackers/:trackerId',
        loadComponent: () =>
          import('./tracker-details/tracker-details.page').then((m) => m.TrackerDetailsPage),
      },
      {
        path: 'license',
        loadChildren: () =>
          import('../../pages/license/license.routes').then((m) => m.LICENSE_ROUTES),
      },
      ...DefaultRoutes,
    ],
  },
  {
    path: '**',
    redirectTo: '/',
  },
];
