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
    path: 'join',
    data: { title: 'Wait List' },
    loadChildren: () =>
      import('../pages/wait-list/wait-list-page.module').then(
        (m) => m.WaitListPageModule,
      ),
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
