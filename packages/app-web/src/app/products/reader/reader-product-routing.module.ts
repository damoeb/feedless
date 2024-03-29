import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ReaderProductPage } from './reader-product.page';
import { ReaderMenuComponent } from './reader-menu/reader-menu.component';

const routes: Routes = [
  {
    path: ':url',
    component: ReaderProductPage,
  },
  {
    path: '',
    component: ReaderProductPage,
  },
  {
    path: '**',
    redirectTo: '/',
  },
  {
    path: '',
    outlet: 'sidemenu',
    component: ReaderMenuComponent,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ReaderProductRoutingModule {}
