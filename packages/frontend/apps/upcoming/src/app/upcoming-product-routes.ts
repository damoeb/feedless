import { Routes } from '@angular/router';
import { template } from 'typesafe-routes';
import { AuthGuardService } from '@feedless/data-access-auth';
import { upcomingBaseRoute } from '@feedless/upcoming-util';
import { eventDetailResolver, eventsResolver } from '@feedless/upcoming-data-access';

export const UPCOMING_ROUTES: Routes = [
  // {
  //   path: '',
  //   outlet: 'sidemenu',
  //   component: FeedlessMenuComponent,
  // },
  {
    path: template(upcomingBaseRoute._.about),
    loadComponent: () =>
      import('@feedless/upcoming-feature-static').then((m) => m.AboutUsPage),
  },
  {
    path: template(upcomingBaseRoute._.terms),
    loadComponent: () =>
      import('@feedless/upcoming-feature-static').then((m) => m.TermsPage),
  },
  {
    path: template(upcomingBaseRoute._.login),
    loadComponent: () =>
      import('@feedless/feature-login').then((m) => m.LoginPage),
  },
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () =>
      import('@feedless/upcoming-feature-event-calendar').then(
        (m) => m.EventCalendarPage,
      ),
  },
  {
    path: template(upcomingBaseRoute._.profile),
    canActivate: [AuthGuardService],
    loadComponent: () =>
      import('@feedless/upcoming-feature-account').then((m) => m.ProfilePage),
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'sources',
      },
      {
        path: template(upcomingBaseRoute.profile._.sources),
        data: { sources: true },
        loadComponent: () =>
          import('@feedless/upcoming-feature-account').then(
            (m) => m.EventSourcesPage,
          ),
      },
      {
        path: template(upcomingBaseRoute.profile._.account),
        loadComponent: () =>
          import('@feedless/upcoming-feature-account').then((m) => m.AccountPage),
      },
      {
        path: template(upcomingBaseRoute.profile._.security),
        loadComponent: () =>
          import('@feedless/upcoming-feature-account').then((m) => m.SecurityPage),
      },
      {
        path: template(upcomingBaseRoute.profile._.subscriptions),
        loadComponent: () =>
          import('@feedless/upcoming-feature-account').then(
            (m) => m.SubscriptionsPage,
          ),
      },
    ],
  },
  {
    path: template(upcomingBaseRoute._.events.countryCode),
    loadComponent: () =>
      import('@feedless/upcoming-feature-hubs').then(
        (m) => m.CountryHubPage,
      ),
  },
  {
    path: template(upcomingBaseRoute._.events.countryCode.region),
    loadComponent: () =>
      import('@feedless/upcoming-feature-hubs').then((m) => m.RegionHubPage),
  },
  {
    resolve: {
      eventDetail: eventDetailResolver,
    },
    path: template(upcomingBaseRoute._.events.countryCode.region.place.event),
    loadComponent: () =>
      import('@feedless/upcoming-feature-event-detail').then(
        (m) => m.EventDetailPage,
      ),
  },
  {
    resolve: {
      events: eventsResolver,
    },
    path: template(upcomingBaseRoute._.events.countryCode.region.place),
    loadComponent: () =>
      import('@feedless/upcoming-feature-event-calendar').then(
        (m) => m.EventCalendarPage,
      ),
  },
];
