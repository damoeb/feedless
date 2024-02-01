import { Injectable } from '@angular/core';
import { Router, Routes } from '@angular/router';
import { AuthGuardService } from '../guards/auth-guard.service';
import { environment } from '../../environments/environment';
import { Title } from '@angular/platform-browser';
import { GqlProductName } from '../../generated/graphql';
import { ReplaySubject } from 'rxjs';

// see https://ionicframework.com/docs/api/split-pane#setting-breakpoints
export type SidemenuBreakpoint = 'xs' | 'sm' | 'md' | 'lg' | 'xl';

export interface SideMenuConfig {
  width: number;
  breakpoint?: SidemenuBreakpoint;
}

export interface ProductConfig {
  product: GqlProductName;
  titlePlain: string;
  titleHtml: string;
  pageTitle: string;
  sideMenu?: SideMenuConfig;
  routes: Routes;
  pages?: { title: string, url: string }[]
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  public static readonly defaultRoutes: Routes = [
    {
      path: 'login',
      loadChildren: () =>
        import('../pages/login/login.module').then((m) => m.EmailLoginPageModule)
    },
    {
      path: 'contact',
      loadChildren: () =>
        import('../pages/contact/contact.module').then(
          (m) => m.ContactPageModule
        )
    },
    {
      path: 'profile',
      canActivate: [AuthGuardService],
      loadChildren: () =>
        import('../pages/profile/profile.module').then(
          (m) => m.ProfilePageModule
        )
    },
    {
      path: 'terms',
      loadChildren: () =>
        import('../pages/terms/terms.module').then((m) => m.TermsPageModule)
    },
    {
      path: 'privacy',
      loadChildren: () =>
        import('../pages/privacy/privacy.module').then(
          (m) => m.PrivacyPageModule
        )
    }
  ];

  private products: ProductConfig[] = [
    {
      product: GqlProductName.Reader,
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
            import('../products/reader/reader-product.module').then(
              (m) => m.ReaderProductModule
            )
        }
      ]
    },
    {
      product: GqlProductName.Upcoming,
      titlePlain: 'Upcoming',
      titleHtml: '<strong>Up</strong><em>coming</em>',
      pageTitle: 'Upcoming',
      routes: [
        {
          path: '',
          loadChildren: () =>
            import('../products/upcoming/upcoming-product.module').then(
              (m) => m.UpcomingProductModule
            )
        }
      ]
    },
    {
      product: GqlProductName.RssBuilder,
      titlePlain: 'RSS Builder',
      titleHtml: '<strong>RSS</strong><em>Builder</em>',
      pageTitle: 'RSS Builder',
      sideMenu: {
        width: 200,
        breakpoint: 'xl'
      },
      routes: [
        {
          path: '',
          loadChildren: () =>
            import('../products/rss-builder/rss-builder-product.module').then(
              (m) => m.RssBuilderProductModule
            )
        }
      ]
    },
    {
      product: GqlProductName.Feedless,
      titlePlain: 'feedless',
      titleHtml: '<strong>feed</strong><em>less</em>',
      pageTitle: 'feedless',
      sideMenu: {
        width: 200,
        breakpoint: 'xl'
      },
      routes: [
        {
          path: '',
          loadChildren: () =>
            import('../products/feedless/feedless-product.module').then(
              (m) => m.FeedlessProductModule
            )
        }
      ]
    },
    {
      product: GqlProductName.VisualDiff,
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
            import('../products/visual-diff/visual-diff-product.module').then(
              (m) => m.VisualDiffProductModule
            )
        }
      ]
    },
  ];

  private activeProductConfig = new ReplaySubject<ProductConfig>()

  constructor(
    private readonly router: Router,
    private readonly titleService: Title
  ) {
  }

  activateProduct(product: GqlProductName) {
    console.log(`activateProduct ${product}`);
    environment.product = () => product;
    const config = this.products.find(
      (productConfig) => productConfig.product === environment.product()
    );
    this.titleService.setTitle(config.pageTitle);
    this.router.resetConfig(config.routes);
    this.activeProductConfig.next(config)
  }

  getActiveProductConfigChange() {
    return this.activeProductConfig.asObservable()
  }

  // forceProduct(product: GqlProduct) {
  //   if (!environment.production) {
  //     this.activateProduct(product);
  //     this.router.navigateByUrl(location.pathname)
  //   }
  // }
}
