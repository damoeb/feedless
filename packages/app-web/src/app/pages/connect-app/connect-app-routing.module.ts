import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';



const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./connect-app.page').then(m => m.ConnectAppPage),
  },
  {
    path: ':link',
    loadComponent: () => import('./connect-app.page').then(m => m.ConnectAppPage),
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ConnectAppPageRoutingModule {}
