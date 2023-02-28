import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';
import { FeatureGuardService } from './guards/feature-guard.service';
import { AuthGuardService } from './guards/auth-guard.service';

const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'buckets',
  },
  {
    path: '',
    canActivate: [FeatureGuardService, AuthGuardService],
    children: [
      {
        path: 'buckets',
        pathMatch: 'full',
        loadChildren: () =>
          import('./pages/buckets/buckets.module').then(
            (m) => m.BucketsPageModule
          ),
      },
      {
        path: 'buckets/:id',
        loadChildren: () =>
          import('./pages/bucket/bucket.module').then(
            (m) => m.BucketPageModule
          ),
      },
      {
        path: 'buckets/new',
        loadChildren: () =>
          import('./components/bucket-create/bucket-create.module').then(
            (m) => m.BucketCreatePageModule
          ),
      },
      {
        path: 'article/:articleId',
        loadChildren: () =>
          import('./pages/article/article.module').then(
            (m) => m.ArticlePageModule
          ),
      },
      {
        path: 'article/:articleId',
        loadChildren: () =>
          import('./pages/article/article.module').then(
            (m) => m.ArticlePageModule
          ),
      },
      {
        path: 'editor/:articleId',
        loadChildren: () =>
          import('./pages/article-editor/article-editor.module').then(
            (m) => m.ArticleEditorPageModule
          ),
      },
      {
        path: 'buckets/:id/importers',
        loadChildren: () =>
          import('./pages/importers/importers.module').then(
            (m) => m.ImportersModule
          ),
      },
      {
        path: 'importer/:id',
        loadChildren: () =>
          import('./components/importer-edit/importer-edit.module').then(
            (m) => m.ImporterEditPageModule
          ),
      },
      {
        path: 'feeds/:id',
        loadChildren: () =>
          import('./pages/feed/feed.module').then((m) => m.FeedPageModule),
      },
      {
        path: 'generic-feeds/:id',
        loadChildren: () =>
          import('./pages/generic-feed/generic-feed.module').then(
            (m) => m.GenericFeedPageModule
          ),
      },
      {
        path: 'feeds',
        loadChildren: () =>
          import('./pages/feeds/feeds.module').then((m) => m.FeedsPageModule),
      },
    ],
  },
  {
    path: 'wizard',
    loadChildren: () =>
      import('./pages/discovery-wizard/discovery-wizard.module').then(
        (m) => m.DiscoveryWizardPageModule
      ),
  },
  {
    path: 'settings',
    canActivate: [FeatureGuardService, AuthGuardService],
    loadChildren: () =>
      import('./pages/settings/settings.module').then(
        (m) => m.SettingsPageModule
      ),
  },
  {
    path: 'profile',
    canActivate: [AuthGuardService],
    loadChildren: () =>
      import('./pages/profile/profile.module').then((m) => m.ProfilePageModule),
  },
  {
    path: '**',
    redirectTo: 'wizard',
  },
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, { preloadingStrategy: PreloadAllModules }),
  ],
  exports: [RouterModule],
})
export class AppRoutingModule {}
