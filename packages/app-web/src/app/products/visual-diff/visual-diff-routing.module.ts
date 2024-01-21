import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { VisualDiffPage } from './visual-diff.page';
import { SubscriptionCreatePage } from './subscription-create/subscription-create.page';
import { SubscriptionsPage } from './subscriptions/subscriptions.page';
import { SubscriptionDetailsPage } from './subscription-details/subscription-details.page';

const routes: Routes = [
  {
    path: '',
    component: VisualDiffPage,
    children: [
      {
        path: '',
        component: SubscriptionCreatePage,
      },
      {
        path: 'new',
        component: SubscriptionCreatePage,
      },
      {
        path: 's',
        component: SubscriptionsPage,
      },
      {
        path: 's/:id',
        component: SubscriptionDetailsPage,
      }
    ]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class VisualDiffPageRoutingModule {}
