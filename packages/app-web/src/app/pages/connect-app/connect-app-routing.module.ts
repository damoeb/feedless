import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ConnectAppPage } from './connect-app.page';

const routes: Routes = [
  {
    path: '',
    component: ConnectAppPage,
  },
  {
    path: ':link',
    component: ConnectAppPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ConnectAppPageRoutingModule {}
