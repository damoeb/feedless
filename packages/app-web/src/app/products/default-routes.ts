import { Routes } from '@angular/router';
import { AuthGuardService } from '../guards/auth-guard.service';

export const DefaultRoutes: Routes = [
  {
    path: 'login',
    loadChildren: () =>
      import('../pages/login/login.module').then((m) => m.EmailLoginPageModule),
  },
  {
    path: 'contact',
    loadChildren: () =>
      import('../pages/contact/contact.module').then(
        (m) => m.ContactPageModule,
      ),
  },
  {
    path: 'feeds/:feedId',
    loadChildren: () =>
      import('../pages/feed-details/feed-details.module').then(
        (m) => m.FeedDetailsPageModule,
      ),
  },
  {
    path: 'feeds',
    canActivate: [AuthGuardService],
    loadChildren: () =>
      import('../pages/feeds/feeds.module').then((m) => m.FeedsPageModule),
  },
  {
    path: 'profile',
    canActivate: [AuthGuardService],
    loadChildren: () =>
      import('../pages/profile/profile.module').then(
        (m) => m.ProfilePageModule,
      ),
  },
  {
    path: 'terms',
    loadChildren: () =>
      import('../pages/terms/terms.module').then((m) => m.TermsPageModule),
  },
  {
    path: 'privacy',
    loadChildren: () =>
      import('../pages/privacy/privacy.module').then(
        (m) => m.PrivacyPageModule,
      ),
  },
];
