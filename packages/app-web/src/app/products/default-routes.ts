import { Routes } from '@angular/router';
import { AuthGuardService } from '../guards/auth-guard.service';
import { ProfileGuardService } from '../guards/profile-guard.service';
import { SaasGuardService } from '../guards/saas-guard.service';

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
    path: 'notebook',
    loadChildren: () =>
      import('../pages/notebook-details/notebook-details.module').then(
        (m) => m.NotebookDetailsPageModule,
      ),
  },
  {
    path: '',
    canActivate: [SaasGuardService],
    children: [
      {
        path: 'directory',
        loadChildren: () =>
          import('../pages/directory/directory.module').then(
            (m) => m.DirectoryPageModule,
          ),
      },
      {
        path: 'connect-app',
        canActivate: [AuthGuardService],
        loadChildren: () =>
          import('../pages/connect-app/connect-app.module').then(
            (m) => m.LinkAppPageModule,
          ),
      },
      {
        path: 'pricing',
        loadChildren: () =>
          import('../pages/pricing/pricing.module').then(
            (m) => m.PricingPageModule,
          ),
      },
      {
        path: 'billings',
        // canActivate: [IsRootGuardService],
        loadChildren: () =>
          import('../pages/billings/billings.module').then(
            (m) => m.BillingsPageModule,
          ),
      },
      {
        path: 'checkout',
        loadChildren: () =>
          import('../pages/checkout/checkout.module').then(
            (m) => m.CheckoutPageModule,
          ),
      },
      {
        path: 'payment/summary',
        loadChildren: () =>
          import('../pages/payment-summary/payment-summary.module').then(
            (m) => m.PaymentSummaryPageModule,
          ),
      },
      {
        path: 'payment',
        loadChildren: () =>
          import('../pages/payment/payment.module').then(
            (m) => m.PaymentPageModule,
          ),
      },
    ],
  },
  {
    path: '',
    canActivate: [AuthGuardService, ProfileGuardService],
    children: [
      {
        path: 'agents',
        loadChildren: () =>
          import('../pages/agents/agents.module').then(
            (m) => m.AgentsPageModule,
          ),
      },
      {
        path: 'secrets',
        loadChildren: () =>
          import('../pages/secrets/secrets.module').then(
            (m) => m.SecretsPageModule,
          ),
      },
      {
        path: 'settings',
        loadChildren: () =>
          import('../pages/settings/settings.module').then(
            (m) => m.SettingsPageModule,
          ),
      },
      {
        path: 'feeds',
        loadChildren: () =>
          import('../pages/feeds/feeds.module').then((m) => m.FeedsPageModule),
      },
      {
        path: 'profile',
        loadChildren: () =>
          import('../pages/profile/profile.module').then(
            (m) => m.ProfilePageModule,
          ),
      },
    ],
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
  {
    path: 'license',
    // canActivate: [SelfHostingGuardService],
    loadChildren: () =>
      import('../pages/license/license.module').then(
        (m) => m.LicensePageModule,
      ),
  },
];
