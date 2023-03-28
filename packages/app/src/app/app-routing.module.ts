import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';
import { AuthGuardService } from './guards/auth-guard.service';
import { environment } from '../environments/environment';

const routes: Routes = [
  {
    path: 'login',
    loadChildren: () =>
      import('./pages/login/login.module').then((m) => m.LoginPageModule),
  },
  {
    path: 'contact',
    loadChildren: () =>
      import('./pages/contact/contact.module').then((m) => m.ContactPageModule),
  },
  {
    path: 'plans',
    loadChildren: () =>
      import('./pages/plans/plans.module').then((m) => m.PlansPageModule),
  },
  {
    path: 'getting-started',
    loadChildren: () =>
      import('./pages/getting-started/getting-started.module').then(
        (m) => m.GettingStartedPageModule
      ),
  },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'getting-started',
  },
  {
    path: '',
    canActivate: [AuthGuardService],
    children: [
      {
        path: '',
        loadChildren: () =>
          import('./pages/buckets/buckets.module').then(
            (m) => m.BucketsPageModule
          ),
      },
      {
        path: 'profile',
        loadChildren: () =>
          import('./pages/profile/profile.module').then(
            (m) => m.ProfilePageModule
          ),
      },
      {
        path: 'article',
        loadChildren: () =>
          import('./pages/article/article.module').then(
            (m) => m.ArticlePageModule
          ),
      },
      {
        path: 'feeds',
        loadChildren: () =>
          import('./pages/feeds/feeds.module').then((m) => m.FeedsPageModule),
      },
      {
        path: 'notifications',
        loadChildren: () =>
          import('./pages/notifications/notifications.module').then(
            (m) => m.NotificationsPageModule
          ),
      },
    ],
  },
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, {
      preloadingStrategy: PreloadAllModules,
      useHash: environment.production,
    }),
  ],
  exports: [RouterModule],
})
export class AppRoutingModule {}
