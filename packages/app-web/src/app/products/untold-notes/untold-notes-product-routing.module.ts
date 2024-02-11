import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UntoldNotesProductPage } from './untold-notes-product.page';
import { ProductService } from '../../services/product.service';
import { UntoldNotesMenuComponent } from './untold-notes-menu/untold-notes-menu.component';

const routes: Routes = [
  {
    path: '',
    outlet: 'sidemenu',
    component: UntoldNotesMenuComponent,
  },
  {
    path: '',
    component: UntoldNotesProductPage,
    children: [
      {
        path: '',
        loadChildren: () =>
          import('./about/about-untold-notes.module').then(
            (m) => m.AboutUntoldNotesModule,
          ),
      },
    ],
  },
  {
    path: 'notebook',
    loadChildren: () =>
      import('./notebook-details/notebook-details.module').then(
        (m) => m.NotebookDetailsPageModule,
      ),
  },
  ...ProductService.defaultRoutes,
  {
    path: '**',
    redirectTo: '/',
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class UntoldNotesPageRoutingModule {}
