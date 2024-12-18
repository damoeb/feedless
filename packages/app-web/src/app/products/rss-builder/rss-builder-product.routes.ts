import { Routes } from '@angular/router';

import { AuthGuardService } from '../../guards/auth-guard.service';
import { DefaultRoutes } from '../default-routes';
import { ProfileGuardService } from '../../guards/profile-guard.service';

export const RSS_BUILDER_ROUTES: Routes = [
  {
    path: 'setup',
    loadComponent: () =>
      import('./self-hosting-setup/self-hosting-setup.page').then(
        (m) => m.SelfHostingSetupPage,
      ),
  },
  {
    path: '',
    loadComponent: () =>
      import('./rss-builder-product.page').then((m) => m.RssBuilderProductPage),
    children: [
      {
        path: '',
        canActivate: [ProfileGuardService],
        children: [
          {
            path: '',
            loadComponent: () =>
              import('./about/about-rss-builder.page').then(
                (m) => m.AboutRssBuilderPage,
              ),
          },
          {
            path: 'feed-builder',
            data: { compact: true, standalone: true },
            // canActivate: [AuthGuardService],
            loadChildren: () =>
              import('../../pages/feed-builder/feed-builder.routes').then(
                (m) => m.FEED_BUILDER_ROUTES,
              ),
          },
          {
            path: 'agents',
            canActivate: [AuthGuardService],
            loadChildren: () =>
              import('../../pages/agents/agents.routes').then(
                (m) => m.AGENTS_ROUTES,
              ),
          },
        ],
      },
      {
        path: '',
        children: [...DefaultRoutes],
      },
    ],
  },
  {
    path: '**',
    redirectTo: '/',
  },
];
