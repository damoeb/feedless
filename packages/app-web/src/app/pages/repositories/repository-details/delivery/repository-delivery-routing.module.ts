import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { RepositoryDeliveryPage } from './repository-delivery.page';

const routes: Routes = [
  {
    path: '',
    component: RepositoryDeliveryPage
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RepositoryDeliveryPageRoutingModule {
}
