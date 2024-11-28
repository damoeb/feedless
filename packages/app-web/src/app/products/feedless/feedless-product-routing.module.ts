import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';



import { DefaultRoutes } from '../default-routes';
import { ProfileGuardService } from '../../guards/profile-guard.service';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./feedless-product.page').then(m => m.FeedlessProductPage),
    children: [
      {
        path: '',
        canActivate: [ProfileGuardService],
        children: [
          {
            path: 'feed-builder',
            loadChildren: () =>
              import('../../pages/feed-builder/feed-builder.module').then(
                (m) => m.FeedBuilderPageModule,
              ),
          },
          {
            path: 'tracker-builder',
            // canActivate: [AuthGuardService],
            loadChildren: () =>
              import('../../pages/tracker-edit/tracker-edit.module').then(
                (m) => m.TrackerEditPageModule,
              ),
          },
          {
            path: 'notebooks',
            // canActivate: [AuthGuardService],
            loadChildren: () =>
              import('../../pages/notebooks/notebooks.module').then(
                (m) => m.NotebooksBuilderPageModule,
              ),
          },
          {
            path: 'workflow-builder',
            loadChildren: () =>
              import(
                '../../pages/workflow-builder/workflow-builder.module'
              ).then((m) => m.WorkflowBuilderPageModule),
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

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class FeedlessProductRoutingModule {}
