import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { FeedlessProductPage } from './feedless-product.page';
import { ProductService } from '../../services/product.service';

const routes: Routes = [
  ...ProductService.defaultRoutes,
  {
    path: '',
    component: FeedlessProductPage,
    children: [
      {
        path: 'builder',
        loadChildren: () =>
          import('../../pages/feed-builder/feed-builder.module').then(
            (m) => m.FeedBuilderPageModule)
      },
      {
        path: 'repositories',
        data: {title: 'Repositories'},
        loadChildren: () =>
          import('../../pages/repositories/repositories.module').then(
            (m) => m.RepositoriesPageModule
          )
      },
      {
        path: 'plans',
        data: {title: 'Plans'},
        loadChildren: () =>
          import('./plans/plans.module').then(
            (m) => m.PlansPageModule
          )
      },
      {
        path: '',
        loadChildren: () =>
          import('./about/about-feedless.module').then(
            (m) => m.AboutFeedlessModule
          )
      },
      {
        path: '**',
        redirectTo: ''
      }
      // {
      //   path: 'agents',
      //   loadChildren: () =>
      //     import('../pages/agents/agents.module').then((m) => m.AgentsPageModule),
      // },
      // {
      //   path: 'sources',
      //
      // }
    ]
  },
  {
    path: '**',
    redirectTo: '/'
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FeedlessProductRoutingModule {
}
