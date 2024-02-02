import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { WaitListPage } from './wait-list.page';

const routes: Routes = [
  {
    path: '',
    component: WaitListPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class WaitListRoutingModule {}
