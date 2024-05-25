import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { RssBuilderProductPage } from './rss-builder-product.page';
import { RssBuilderMenuComponent } from './rss-builder-menu/rss-builder-menu.component';
import { AuthGuardService } from '../../guards/auth-guard.service';
import { DefaultRoutes } from '../default-routes';
import { SaasGuardService } from '../../guards/saas-guard.service';
import { SelfHostingGuardService } from '../../guards/self-hosting-guard.service';

const routes: Routes = [
  {
    path: '',
    outlet: 'sidemenu',
    component: RssBuilderMenuComponent,
  },
  {
    path: '',
    component: RssBuilderProductPage,
    children: [
      {
        path: '',
        loadChildren: () =>
          import('./about/about-rss-builder.module').then(
            (m) => m.AboutRssBuilderModule,
          ),
      },
      {
        path: 'builder',
        canActivate: [AuthGuardService],
        loadChildren: () =>
          import('../../pages/feed-builder/feed-builder.module').then(
            (m) => m.FeedBuilderPageModule,
          ),
      },
      {
        path: 'feeds/:feedId',
        loadChildren: () =>
          import('./feed-details/feed-details.module').then(
            (m) => m.FeedDetailsPageModule,
          ),
      },
      {
        path: 'feeds',
        canActivate: [AuthGuardService],
        loadChildren: () =>
          import('./feeds/feeds.module').then((m) => m.FeedsPageModule),
      },
      {
        path: 'agents',
        canActivate: [AuthGuardService],
        loadChildren: () =>
          import('../../pages/agents/agents.module').then(
            (m) => m.AgentsPageModule,
          ),
      },
      {
        path: 'plans',
        canActivate: [SaasGuardService],
        loadChildren: () =>
          import('./plans/plans.module').then((m) => m.PlansPageModule),
      },
      {
        path: 'secrets',
        canActivate: [AuthGuardService],
        loadChildren: () =>
          import('../../pages/secrets/secrets.module').then(
            (m) => m.SecretsPageModule,
          ),
      },
      {
        path: 'settings',
        canActivate: [AuthGuardService],
        loadChildren: () =>
          import('../../pages/settings/settings.module').then(
            (m) => m.SettingsPageModule,
          ),
      },
      {
        path: 'license',
        canActivate: [SelfHostingGuardService],
        loadChildren: () =>
          import('../../pages/license/license.module').then(
            (m) => m.LicensePageModule,
          ),
      },
      ...DefaultRoutes,
    ],
  },
  {
    path: '**',
    redirectTo: '/',
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class RssBuilderPageRoutingModule {}
