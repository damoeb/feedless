import { inject, Injectable } from '@angular/core';
import { Router, Routes } from '@angular/router';
import { AuthGuardService } from '../guards/auth-guard.service';
import { FallbackRedirectService } from '../guards/fallback-redirect.service';
import { environment } from '../../environments/environment';
import { Title } from '@angular/platform-browser';
import { GqlProduct } from '../../generated/graphql';

// see https://ionicframework.com/docs/api/split-pane#setting-breakpoints
export type SidemenuBreakpoint = 'xs' | 'sm' | 'md' | 'lg' | 'xl';

export interface SideMenuConfig {
  width: number;
  breakpoint?: SidemenuBreakpoint;
}

export interface ProductConfig {
  product: GqlProduct;
  titlePlain: string;
  titleHtml: string;
  pageTitle: string;
  sideMenu?: SideMenuConfig;
  routes: Routes;
}

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  public static readonly defaultRoutes: Routes = [
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
      titlePlain: 'Reader',
      titleHtml: '<strong>Reader</strong>',
      pageTitle: 'Reader',
      sideMenu: {
        width: 200,
        breakpoint: 'md'
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
      titlePlain: 'RSS Builder',
      titleHtml: '<strong>RSS</strong><em>Builder</em>',
      pageTitle: 'RSS Builder',
      sideMenu: {
        width: 300,
        breakpoint: 'xl'
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
      titlePlain: 'feedless',
      titleHtml: 'feedless',
      pageTitle: 'feedless',
      sideMenu: {
        width: 200,
      },
      routes: [
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
      titlePlain: 'VisualDiff',
      titleHtml: '<strong>Visual</strong><em>Diff</em>',
      pageTitle: 'VisualDiff',
      sideMenu: {
        width: 200,
        breakpoint: 'lg'
      },
      routes: [
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
    // todo mag default routes must be child of product
  }

  getProductConfig(): ProductConfig {
    return this.products.find(
      (product) => product.product === environment.product(),
    );
  }
}
