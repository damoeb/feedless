import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SubscriptionDetailsPage } from './subscription-details.page';

const routes: Routes = [
  {
    path: '',
    component: SubscriptionDetailsPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class SubscriptionDetailsRoutingModule {}
