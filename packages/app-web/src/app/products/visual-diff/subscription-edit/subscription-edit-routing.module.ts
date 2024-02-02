import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SubscriptionEditPage } from './subscription-edit.page';

const routes: Routes = [
  {
    path: '',
    component: SubscriptionEditPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class SubscriptionEditRoutingModule {}
