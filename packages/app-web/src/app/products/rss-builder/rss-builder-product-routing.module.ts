import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';


import { AuthGuardService } from '../../guards/auth-guard.service';
import { DefaultRoutes } from '../default-routes';
import { ProfileGuardService } from '../../guards/profile-guard.service';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./rss-builder-product.page').then(m => m.RssBuilderProductPage),
    children: [
      {
        path: '',
        canActivate: [ProfileGuardService],
        children: [
          {
            path: '',
            loadChildren: () =>
              import('./about/about-rss-builder.module').then(
                (m) => m.AboutRssBuilderModule,
              ),
          },
          {
            path: 'feed-builder',
            canActivate: [AuthGuardService],
            loadChildren: () =>
              import('../../pages/feed-builder/feed-builder.module').then(
                (m) => m.FeedBuilderPageModule,
              ),
          },
          {
            path: 'agents',
            canActivate: [AuthGuardService],
            loadChildren: () =>
              import('../../pages/agents/agents.module').then(
                (m) => m.AgentsPageModule,
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

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class RssBuilderPageRoutingModule {}
