import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { VisualDiffPage } from './visual-diff.page';
import { SubscriptionEditPage } from './subscription-edit/subscription-edit.page';
import { SubscriptionsPage } from './subscriptions/subscriptions.page';
import { SubscriptionDetailsPage } from './subscription-details/subscription-details.page';
import { ProductService } from '../../services/product.service';

const routes: Routes = [
  {
    path: '',
    component: VisualDiffPage,
    children: [
      {
        path: '',
        component: SubscriptionEditPage,
      },
      {
        path: 'new',
        component: SubscriptionEditPage,
      },
      {
        path: 's',
        component: SubscriptionsPage,
      },
      {
        path: 's/:id',
        component: SubscriptionDetailsPage,
      },
      ...ProductService.defaultRoutes,
    ]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class VisualDiffPageRoutingModule {}
