import { inject, Injectable } from '@angular/core';
import { Router, Routes } from '@angular/router';
import { AuthGuardService } from '../guards/auth-guard.service';
import { FallbackRedirectService } from '../guards/fallback-redirect.service';
import { environment } from '../../environments/environment';
import { Title } from '@angular/platform-browser';
import { GqlProduct } from '../../generated/graphql';
import { AuthService } from './auth.service';

// see https://ionicframework.com/docs/api/split-pane#setting-breakpoints
export type SidemenuBreakpoint = 'xs' | 'sm' | 'md' | 'lg' | 'xl';

export interface SideMenuConfig {
  width: number;
  breakpoint?: SidemenuBreakpoint;
}

export interface ProductConfig {
  product: GqlProduct;
  name: string;
  pageTitle: string;
  sideMenu?: SideMenuConfig;
  routes: Routes;
}

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  private routes: Routes = [
    {
      path: 'login',
      loadChildren: () =>
        import('../pages/login/login.module').then((m) => m.LoginPageModule),
    },
    {
      path: 'contact',
      loadChildren: () =>
        import('../pages/contact/contact.module').then(
          (m) => m.ContactPageModule,
        ),
    },
    {
      path: 'plans',
      loadChildren: () =>
        import('../pages/plans/plans.module').then((m) => m.PlansPageModule),
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
    {
      path: 'notifications',
      canActivate: [AuthGuardService],
      loadChildren: () =>
        import('../pages/notifications/notifications.module').then(
          (m) => m.NotificationsPageModule,
        ),
    },
  ];

  private products: ProductConfig[] = [
    {
      product: GqlProduct.Reader,
      name: 'Reader',
      pageTitle: 'Reader',
      sideMenu: {
        width: 300,
      },
      routes: [
        {
          path: '',
          loadChildren: () =>
            import('../products/reader/reader.module').then(
              (m) => m.ReaderPageModule,
            ),
        },
      ],
    },
    {
      product: GqlProduct.RssBuilder,
      name: 'RSS Builder',
      pageTitle: 'RSS Builder',
      sideMenu: {
        width: 300,
        breakpoint: 'lg'
      },
      routes: [
        {
          path: '',
          loadChildren: () =>
            import('../products/rss-builder/rss-builder.module').then(
              (m) => m.RssBuilderPageModule,
            ),
        },
      ],
    },
    {
      product: GqlProduct.Feedless,
      name: 'feedless',
      pageTitle: 'feedless',
      sideMenu: {
        width: 200,
      },
      routes: [
        ...this.routes,
        {
          path: 'agents',
          loadChildren: () =>
            import('../pages/agents/agents.module').then((m) => m.AgentsPageModule),
        },
        {
          path: 'getting-started',
          loadChildren: () =>
            import('../pages/getting-started/getting-started.module').then(
              (m) => m.GettingStartedPageModule,
            ),
        },
        {
          path: 'sources',
          loadChildren: () =>
            import('../pages/sources/sources.module').then(
              (m) => m.SourcesPageModule,
            ),
        },
        {
          path: '',
          canActivate: [() => inject(FallbackRedirectService).canActivate()],
          children: [],
        },
      ],
    },
    {
      product: GqlProduct.VisualDiff,
      name: 'VisualDiff',
      pageTitle: 'VisualDiff',
      sideMenu: {
        width: 200,
        breakpoint: 'lg'
      },
      routes: [
        ...this.routes,
        {
          path: '',
          // canMatch: [() => true],
          loadChildren: () =>
            import('../products/visual-diff/visual-diff.module').then(
              (m) => m.VisualDiffPageModule,
            ),
        },
      ],
    },
  ];

  constructor(
    private readonly router: Router,
    private readonly titleService: Title,
  ) {}

  resolveProduct(product: GqlProduct) {
    environment.product = () => product;
    console.log(`resolveProduct ${environment.product()}`);
    const config = this.getProductConfig();
    this.titleService.setTitle(config.pageTitle);
    this.router.resetConfig(config.routes);
  }

  getProductConfig(): ProductConfig {
    return this.products.find(
      (product) => product.product === environment.product(),
    );
  }
}
