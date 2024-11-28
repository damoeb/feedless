import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';




const routes: Routes = [
  {
    path: ':url',
    loadComponent: () => import('./reader-product.page').then(m => m.ReaderProductPage),
  },
  {
    path: '',
    loadComponent: () => import('./reader-product.page').then(m => m.ReaderProductPage),
  },
  {
    path: '**',
    redirectTo: '/',
  },
  {
    path: '',
    outlet: 'sidemenu',
    loadComponent: () => import('./reader-menu/reader-menu.component').then(m => m.ReaderMenuComponent),
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ReaderProductRoutingModule {}
