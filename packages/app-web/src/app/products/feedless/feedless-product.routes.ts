import { Routes } from '@angular/router';

import { DefaultRoutes } from '../default-routes';
import { ProfileGuardService } from '../../guards/profile-guard.service';

export const FEEDLESS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./feedless-product.page').then((m) => m.FeedlessProductPage),
    children: [
      {
        path: '',
        canActivate: [ProfileGuardService],
        children: [
          {
            path: 'feed-builder',
            loadChildren: () =>
              import('../../pages/feed-builder/feed-builder.routes').then(
                (m) => m.FEED_BUILDER_ROUTES,
              ),
          },
          {
            path: 'tracker-builder',
            // canActivate: [AuthGuardService],
            loadChildren: () =>
              import('../../pages/tracker-edit/tracker-edit.routes').then(
                (m) => m.TRACKER_EDIT_ROUTES,
              ),
          },
          {
            path: 'notebooks',
            // canActivate: [AuthGuardService],
            loadChildren: () =>
              import('../../pages/notebooks/notebooks.routes').then(
                (m) => m.NOTEBOOKS_ROUTING,
              ),
          },
          {
            path: 'workflow-builder',
            loadChildren: () =>
              import(
                '../../pages/workflow-builder/workflow-builder.routes'
              ).then((m) => m.WORKFLOW_BUILDER_ROUTES),
          },
          {
            path: 'products',
            loadChildren: () =>
              import('./products/products-routes').then(
                (m) => m.PRODUCT_ROUTES,
              ),
          },
          {
            path: '',
            loadComponent: () =>
              import('./about/about-feedless.page').then(
                (m) => m.AboutFeedlessPage,
              ),
          },
        ],
      },
      {
        path: '',
        children: [...DefaultRoutes],
      },
      {
        path: '**',
        redirectTo: '',
      },
    ],
  },
  // {
  //   path: '',
  //   outlet: 'sidemenu',
  //   component: FeedlessMenuComponent,
  // },
  {
    path: '**',
    redirectTo: '/',
  },
];
