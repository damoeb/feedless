import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';
import { AuthGuardService } from './guards/auth-guard.service';
import { FallbackRedirectService } from './guards/fallback-redirect.service';

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
    path: 'buckets',
    loadChildren: () =>
      import('./pages/buckets/buckets.module').then((m) => m.BucketsPageModule),
  },
  {
    path: 'profile',
    canActivate: [AuthGuardService],
    loadChildren: () =>
      import('./pages/profile/profile.module').then((m) => m.ProfilePageModule),
  },
  {
    path: 'articles',
    loadChildren: () =>
      import('./pages/articles/articles.module').then(
        (m) => m.ArticlesPageModule
      ),
  },
  {
    path: 'feeds',
    loadChildren: () =>
      import('./pages/feeds/feeds.module').then((m) => m.FeedsPageModule),
  },
  {
    path: 'notifications',
    canActivate: [AuthGuardService],
    loadChildren: () =>
      import('./pages/notifications/notifications.module').then(
        (m) => m.NotificationsPageModule
      ),
  },
  {
    path: 'terms',
    loadChildren: () =>
      import('./pages/terms/terms.module').then((m) => m.TermsPageModule),
  },
  {
    path: '**',
    canActivate: [FallbackRedirectService],
    redirectTo: '',
  },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: '/buckets',
  },
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, {
      preloadingStrategy: PreloadAllModules,
    }),
  ],
  exports: [RouterModule],
})
export class AppRoutingModule {}
