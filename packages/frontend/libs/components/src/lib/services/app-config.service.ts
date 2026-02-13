import { inject, Injectable } from '@angular/core';
import { Routes } from '@angular/router';
import { Title } from '@angular/platform-browser';
import {
  environment,
  VerticalAppConfig,
  VerticalId,
  VerticalSpec,
} from '@feedless/core';
import { omit } from 'lodash-es';

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
  private readonly titleService = inject(Title);

  public activeProductConfig!: VerticalSpecWithRoutes;
  public customProperties!: { [p: string]: number | boolean | string };

  async activateUserInterface(appConfig: VerticalAppConfig) {
    // console.log('appConfig', appConfig);
    const product = appConfig.product;
    const customProperties = omit(
      appConfig,
      'product',
      'offlineSupport',
      'apiUrl',
    );
    // console.log(
    //   `activateUserInterface ${product} with customProperties ${JSON.stringify(customProperties)}`,
    // );
    environment.product = product;
    environment.offlineSupport = appConfig.offlineSupport === true;
    // const appConfigFull = await this.resolveAppConfig(product);
    this.customProperties = customProperties;
    // this.titleService.setTitle(appConfigFull.pageTitle);
    // this.router.resetConfig(appConfigFull.routes);
    // this.activeProductConfig = appConfigFull;
    // this.activeProductConfigSubject.next(appConfigFull);
  }

  setPageTitle(title: string) {
    this.titleService.setTitle(`${title} | ${this.activeProductConfig.title}`);
  }
}
