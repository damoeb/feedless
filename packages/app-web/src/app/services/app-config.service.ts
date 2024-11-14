import { Injectable } from '@angular/core';
import { Router, Routes } from '@angular/router';
import { environment } from '../../environments/environment';
import { Title } from '@angular/platform-browser';
import { GqlVertical } from '../../generated/graphql';
import { ReplaySubject } from 'rxjs';
import { marked } from 'marked';
import { VerticalSpec, allVerticals, VerticalId } from '../all-verticals';
import { omit } from 'lodash-es';
import { VerticalAppConfig } from '../types';

// see https://ionicframework.com/docs/api/split-pane#setting-breakpoints
export type SidemenuBreakpoint = 'xs' | 'sm' | 'md' | 'lg' | 'xl';

export interface SideMenuConfig {
  width: number;
  breakpoint?: SidemenuBreakpoint;
}

export type VerticalSpecWithRoutesAndMarkup = VerticalSpecWithRoutes & {
  localSetup: string;
  videoTeaserSnipped: string;
};
export type VerticalSpecWithRoutes = VerticalRoutes &
  VerticalSpec & { imageUrl: string };

export type VerticalRoutes = {
  id: VerticalId;
  routes: Routes;
  sideMenu?: SideMenuConfig;
};

@Injectable({
  providedIn: 'root',
})
export class AppConfigService {
  private verticalRoutes: VerticalRoutes[] = [
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
      id: 'changeTracker',
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

  private activeProductConfigSubject =
    new ReplaySubject<VerticalSpecWithRoutes>();
  public activeProductConfig!: VerticalSpecWithRoutes;
  public customProperties!: { [p: string]: number | boolean | string };

  constructor(
    private readonly router: Router,
    private readonly titleService: Title,
  ) {}

  async activateUserInterface(appConfig: VerticalAppConfig) {
    console.log('appConfig', appConfig);
    const product = appConfig.product;
    const customProperties = omit(
      appConfig,
      'product',
      'offlineSupport',
      'apiUrl',
    );
    console.log(
      `activateUserInterface ${product} with customProperties ${JSON.stringify(customProperties)}`,
    );
    environment.product = product;
    environment.offlineSupport = appConfig.offlineSupport === true;
    const appConfigFull = await this.resolveAppConfig(product);
    this.customProperties = customProperties;
    this.titleService.setTitle(appConfigFull.pageTitle);
    this.router.resetConfig(appConfigFull.routes);
    this.activeProductConfig = appConfigFull;
    this.activeProductConfigSubject.next(appConfigFull);
  }

  getActiveProductConfigChange() {
    return this.activeProductConfigSubject.asObservable();
  }

  getAllAppConfigs(): Promise<VerticalSpecWithRoutesAndMarkup[]> {
    return Promise.all(
      allVerticals.verticals.map(
        async (meta): Promise<VerticalSpecWithRoutesAndMarkup> => {
          const vertical = this.verticalRoutes.find((p) => meta.id === p.id)!;
          return {
            ...meta,
            videoTeaserSnipped: `<lite-youtube videoid="${meta.videoUrl}"></lite-youtube>`,
            ...vertical,
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

  private async resolveAppConfig(
    product: GqlVertical,
  ): Promise<VerticalSpecWithRoutes> {
    const configs = await this.getAllAppConfigs();
    return configs.find((productConfig) => productConfig.product === product)!;
  }
}
