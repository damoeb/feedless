import { Routes } from '@angular/router';
import { AuthGuardService } from '../guards/auth-guard.service';
import { ProfileGuardService } from '../guards/profile-guard.service';
import { SaasGuardService } from '../guards/saas-guard.service';
import { param } from 'typesafe-routes';

export const upperCaseStringParser = param({
  serialize: (value: string) => value.toUpperCase(),
  parse: (value: string) => value.toUpperCase(),
});

export const DefaultRoutes: Routes = [
  {
    path: 'login',
    loadChildren: () => import('../pages/login/login.routes').then((m) => m.LOGIN_ROUTES),
  },
  {
    path: 'feeds/:feedId',
    loadChildren: () =>
      import('../pages/feed-details/feed-details.routes').then((m) => m.FEED_DETAILS_ROUTES),
  },
  {
    path: 'feeds/:feedId/report',
    loadChildren: () => import('../pages/report/report.routes').then((m) => m.REPORT_ROUTES),
  },
  {
    path: 'notebook',
    loadChildren: () =>
      import('../pages/notebook-details/notebook-details.routes').then(
        (m) => m.NOTEBOOK_DETAILS_ROUTES
      ),
  },
  {
    path: '',
    canActivate: [SaasGuardService],
    children: [
      {
        path: 'directory',
        loadChildren: () =>
          import('../pages/directory/directory.routes').then((m) => m.DIRECTORY_ROUTES),
      },
      {
        path: 'connect-app',
        canActivate: [AuthGuardService],
        loadChildren: () =>
          import('../pages/connect-app/connect-app.routes').then((m) => m.CONNECT_APP_ROUTES),
      },
      {
        path: 'pricing',
        loadChildren: () => import('../pages/pricing/pricing.routes').then((m) => m.PRICING_ROUTES),
      },
      {
        path: 'billings',
        // canActivate: [IsRootGuardService],
        loadChildren: () =>
          import('../pages/billings/billings.routes').then((m) => m.BILLING_ROUTES),
      },
      {
        path: 'checkout',
        loadChildren: () =>
          import('../pages/checkout/checkout.routes').then((m) => m.CHECKOUT_ROUTES),
      },
      {
        path: 'payment/summary',
        loadChildren: () =>
          import('../pages/payment-summary/payment-summary.routes').then(
            (m) => m.PAYMENT_SUMMARY_ROUTES
          ),
      },
      {
        path: 'payment',
        loadChildren: () => import('../pages/payment/payment.routes').then((m) => m.PAYMENT_ROUTES),
      },
    ],
  },
  {
    path: '',
    canActivate: [AuthGuardService, ProfileGuardService],
    children: [
      {
        path: 'agents',
        loadChildren: () => import('../pages/agents/agents.routes').then((m) => m.AGENTS_ROUTES),
      },
      {
        path: 'services',
        loadChildren: () =>
          import('../pages/services/services.routes').then((m) => m.SERVICES_ROUTES),
      },
      {
        path: 'plugins',
        loadChildren: () => import('../pages/plugins/plugins.routes').then((m) => m.PLUGINS_ROUTES),
      },
      {
        path: 'applications',
        loadChildren: () =>
          import('../pages/applications/applications.routes').then((m) => m.APPLICATIONS_ROUTES),
      },
      {
        path: 'subscriptions',
        loadChildren: () =>
          import('../pages/subscriptions/subscriptions.routes').then((m) => m.SUBSCRIPTIONS_ROUTES),
      },
      {
        path: 'settings',
        loadChildren: () =>
          import('../pages/settings/settings.routes').then((m) => m.SETTINGS_ROUTES),
      },
      {
        path: 'feeds',
        loadChildren: () => import('../pages/feeds/feeds.routes').then((m) => m.FEEDS_ROUTES),
      },
      {
        path: 'profile',
        loadChildren: () => import('../pages/profile/profile.routes').then((m) => m.PROFILE_ROUTES),
      },
    ],
  },
  {
    path: 'docs',
    loadChildren: () =>
      import('../pages/documents/documents.routes').then((m) => m.DOCUMENTS_ROUTES),
  },
  {
    path: 'privacy',
    loadChildren: () => import('../pages/privacy/privacy.routes').then((m) => m.PRIVACY_ROUTES),
  },
  {
    path: 'license',
    // canActivate: [SelfHostingGuardService],
    loadChildren: () => import('../pages/license/license.routes').then((m) => m.LICENSE_ROUTES),
  },
];
