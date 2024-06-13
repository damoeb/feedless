import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { FeedlessProductPage } from './feedless-product.page';

import { DefaultRoutes } from '../default-routes';
import { FeedlessMenuComponent } from './feedless-menu/feedless-menu.component';
import { ProfileGuardService } from '../../guards/profile-guard.service';

const routes: Routes = [
  {
    path: '',
    component: FeedlessProductPage,
    children: [
      {
        path: '',
        canActivate: [ProfileGuardService],
        children: [
          {
            path: 'builder',
            loadChildren: () =>
              import('../../pages/feed-builder/feed-builder.module').then(
                (m) => m.FeedBuilderPageModule,
              ),
          },
          {
            path: 'workflow-builder',
            loadChildren: () =>
              import('../../pages/workflow-builder/workflow-builder.module').then(
                (m) => m.WorkflowBuilderPageModule,
              ),
          },
          {
            path: 'products',
            loadChildren: () =>
              import('./products/products.module').then(
                (m) => m.ProductsPageModule,
              ),
          },
          {
            path: '',
            loadChildren: () =>
              import('./about/about-feedless.module').then(
                (m) => m.AboutFeedlessModule,
              ),
          }
        ],
      },
      {
        path: '',
        children: [
          ...DefaultRoutes,
        ],
      },
      {
        path: '**',
        redirectTo: '',
      },
    ],
  },
  {
    path: '',
    outlet: 'sidemenu',
    component: FeedlessMenuComponent,
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
export class FeedlessProductRoutingModule {}
