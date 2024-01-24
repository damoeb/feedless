import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { VisualDiffProductPage } from './visual-diff-product.page';
import { ProductService } from '../../services/product.service';

const routes: Routes = [
  {
    path: '',
    component: VisualDiffProductPage,
    children: [
      {
        path: '',
        data: { title: '' },
          loadChildren: () =>
            import('./about/about-visual-diff.module').then((m) => m.AboutVisualDiffModule)
      },
      {
        path: 'plans',
        data: { title: 'Plans' },
        loadChildren: () =>
          import('./plans/plans.module').then((m) => m.PlansPageModule)
      },
      {
        path: 'builder',
        data: { title: 'Builder' },
        loadChildren: () =>
          import('./subscription-edit/subscription-edit.module').then((m) => m.SubscriptionEditPageModule)

      },
      {
        path: 'trackers',
        data: { title: 'Page Trackers' },
        loadChildren: () =>
          import('./subscriptions/subscriptions.module').then((m) => m.SubscriptionsPageModule)

      },
      {
        path: 'trackers/:id',
        data: { title: 'Page Tracker' },
        loadChildren: () =>
          import('./subscription-details/subscription-details.module').then((m) => m.SubscriptionDetailsPageModule)

      },
      ...ProductService.defaultRoutes,
      {
        path: '**',
        redirectTo: '/'
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class VisualDiffProductRoutingModule {
}
