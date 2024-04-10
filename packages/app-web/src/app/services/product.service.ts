import { Injectable } from '@angular/core';
import { Router, Routes } from '@angular/router';
import { AuthGuardService } from '../guards/auth-guard.service';
import { environment } from '../../environments/environment';
import { Title } from '@angular/platform-browser';
import { GqlProductName } from '../../generated/graphql';
import { ReplaySubject } from 'rxjs';
import { marked } from 'marked';
import { AppConfig, feedlessConfig, ProductId } from '../feedless-config';

// see https://ionicframework.com/docs/api/split-pane#setting-breakpoints
export type SidemenuBreakpoint = 'xs' | 'sm' | 'md' | 'lg' | 'xl';

export interface SideMenuConfig {
  width: number;
  breakpoint?: SidemenuBreakpoint;
}

export type ProductConfig = ProductRoutesConfig &
  AppConfig & { imageUrl: string };

export type ProductRoutesConfig = {
  id: ProductId;
  routes: Routes;
  sideMenu?: SideMenuConfig;
};

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  public static readonly defaultRoutes: Routes = [
    {
      path: 'login',
      loadChildren: () =>
        import('../pages/login/login.module').then(
          (m) => m.EmailLoginPageModule,
        ),
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

  private productRoutes: ProductRoutesConfig[] = [
    {
      id: 'reader',
      // sideMenu: {
      //   width: 200,
      //   breakpoint: 'md',
      // },
      routes: [
        {
          path: '',
          loadChildren: () =>
            import('../products/reader/reader-product.module').then(
              (m) => m.ReaderProductModule,
            ),
        },
      ],
    },
    {
      id: 'upcoming',
      routes: [
        {
          path: '',
          loadChildren: () =>
            import('../products/upcoming/upcoming-product.module').then(
              (m) => m.UpcomingProductModule,
            ),
        },
      ],
    },
    {
      id: 'pageChangeTracker',
      routes: [
        {
          path: '',
          loadChildren: () =>
            import('../products/pc-tracker/pc-tracker-product.module').then(
              (m) => m.PcTrackerProductModule,
            ),
        },
      ],
    },
    {
      id: 'rss-proxy',
      sideMenu: {
        width: 200,
        breakpoint: 'xl',
      },
      routes: [
        {
          path: '',
          loadChildren: () =>
            import('../products/rss-builder/rss-builder-product.module').then(
              (m) => m.RssBuilderProductModule,
            ),
        },
      ],
    },
    {
      id: 'untold',
      sideMenu: {
        width: 200,
        breakpoint: 'xl',
      },
      routes: [
        {
          path: '',
          loadChildren: () =>
            import('../products/untold-notes/untold-notes-product.module').then(
              (m) => m.UntoldNotesProductModule,
            ),
        },
      ],
    },
    {
      id: 'feedless',
      // sideMenu: {
      //   width: 200,
      //   breakpoint: 'xl',
      // },
      routes: [
        {
          path: '',
          loadChildren: () =>
            import('../products/feedless/feedless-product.module').then(
              (m) => m.FeedlessProductModule,
            ),
        },
      ],
    },
    {
      id: 'visual-diff',
      sideMenu: {
        width: 200,
        breakpoint: 'lg',
      },
      routes: [
        {
          path: '',
          // canMatch: [() => true],
          loadChildren: () =>
            import('../products/visual-diff/visual-diff-product.module').then(
              (m) => m.VisualDiffProductModule,
            ),
        },
      ],
    },
  ];

  private activeProductConfig = new ReplaySubject<ProductConfig>();

  constructor(
    private readonly router: Router,
    private readonly titleService: Title,
  ) {}

  async activateProduct(product: GqlProductName) {
    console.log(`activateProduct ${product}`);
    environment.product = product;
    const config = await this.resolveProductConfig(product);
    console.log('productConfig', config);
    environment.offlineSupport = config.offlineSupport === true;
    this.titleService.setTitle(config.pageTitle);
    this.router.resetConfig(config.routes);
    this.activeProductConfig.next(config);
  }

  getActiveProductConfigChange() {
    return this.activeProductConfig.asObservable();
  }

  async getProductConfigs(): Promise<ProductConfig[]> {
    return Promise.all(
      feedlessConfig.apps.map(async (meta) => {
        const ui = this.productRoutes.find((p) => meta.id === p.id);
        return {
          ...meta,
          ...ui,
          localSetup: await marked(meta.localSetup),
          descriptionHtml: await marked(meta.descriptionMarkdown),
          imageUrl: `/assets/${meta.id}.jpeg`,
        };
      }),
    );
  }

  private async resolveProductConfig(
    product: GqlProductName,
  ): Promise<ProductConfig> {
    const configs = await this.getProductConfigs();
    return configs.find((productConfig) => productConfig.product === product);
  }
}
