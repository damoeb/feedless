import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { RepositoryDetailsPage } from './repository-details.page';

const routes: Routes = [
  {
    path: '',
    component: RepositoryDetailsPage,
    children: [
      {
        path: 'data',
        loadChildren: () =>
          import('./data/repository-data.module').then(
            (m) => m.RepositoryDataPageModule,
          ),
      },
      {
        path: 'sources',
        loadChildren: () =>
          import('./sources/repository-sources.module').then(
            (m) => m.RepositorySourcesPageModule,
          ),
      },
      {
        path: 'plugins',
        loadChildren: () =>
          import('./plugins/repository-plugins.module').then(
            (m) => m.RepositoryPluginsPageModule,
          ),
      },
      {
        path: 'delivery',
        loadChildren: () =>
          import('./delivery/repository-delivery.module').then(
            (m) => m.RepositoryDeliveryPageModule,
          ),
      },
      {
        path: 'settings',
        loadChildren: () =>
          import('./settings/repository-settings.module').then(
            (m) => m.RepositorySettingsPageModule,
          ),
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'data',
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class RepositoryDetailsPageRoutingModule {}
