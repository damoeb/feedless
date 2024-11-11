import { Injectable } from '@angular/core';
import { Router, Routes } from '@angular/router';
import { environment } from '../../environments/environment';
import { Title } from '@angular/platform-browser';
import { GqlProductCategory } from '../../generated/graphql';
import { ReplaySubject } from 'rxjs';
import { marked } from 'marked';
import { AppConfig, feedlessConfig, ProductId } from '../feedless-config';
import { WebsiteConfig } from './server-config.service';

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
export class AppConfigService {
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
            import(
              '../products/change-tracker/change-tracker-product.module'
            ).then((m) => m.ChangeTrackerProductModule),
        },
      ],
    },
    {
      id: 'rss-proxy',
      // sideMenu: {
      //   width: 200,
      //   breakpoint: 'xl',
      // },
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
      sideMenu: {
        width: 200,
        breakpoint: 'xs',
      },
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
      // sideMenu: {
      //   width: 200,
      //   breakpoint: 'lg',
      // },
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

  private activeProductConfigSubject = new ReplaySubject<ProductConfig>();
  public activeProductConfig!: ProductConfig;
  public customProperties!: { [p: string]: number | boolean | string };

  constructor(
    private readonly router: Router,
    private readonly titleService: Title,
  ) {}

  async activateUserInterface({ product, customProperties }: WebsiteConfig) {
    console.log(
      `activateUserInterface ${product} with customProperties ${JSON.stringify(customProperties)}`,
    );
    environment.product = product;
    const config = await this.resolveProductConfig(product);
    console.log('productConfig', config);
    this.customProperties = customProperties;
    environment.offlineSupport = config.offlineSupport === true;
    this.titleService.setTitle(config.pageTitle);
    this.router.resetConfig(config.routes);
    this.activeProductConfig = config;
    this.activeProductConfigSubject.next(config);
  }

  getActiveProductConfigChange() {
    return this.activeProductConfigSubject.asObservable();
  }

  getProductConfigs(): Promise<
    (ProductConfig & { localSetup: string; videoTeaserSnipped: string })[]
  > {
    return Promise.all(
      feedlessConfig.apps.map(
        async (
          meta,
        ): Promise<
          ProductConfig & { localSetup: string; videoTeaserSnipped: string }
        > => {
          const ui = this.productRoutes.find((p) => meta.id === p.id)!;
          return {
            ...meta,
            videoTeaserSnipped: `<lite-youtube videoid="${meta.videoUrl}"></lite-youtube>`,
            ...ui,
            localSetup: `${await marked(meta.localSetupBeforeMarkup || '')}
\`\`\`bash
${meta.localSetupBash}
\`\`\`
${await marked(meta.localSetupAfterMarkup || '')}`,
            descriptionHtml: await marked(meta.descriptionMarkdown),
            imageUrl: `/assets/${meta.id}.jpeg`,
          };
        },
      ),
    );
  }

  setPageTitle(title: string) {
    this.titleService.setTitle(`${title} | ${this.activeProductConfig.title}`);
  }

  private async resolveProductConfig(
    product: GqlProductCategory,
  ): Promise<ProductConfig> {
    const configs = await this.getProductConfigs();
    return configs.find((productConfig) => productConfig.product === product)!;
  }
}
