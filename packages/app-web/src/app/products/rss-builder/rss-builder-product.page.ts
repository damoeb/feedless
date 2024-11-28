import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ActivatedRoute, RouterLink, RouterLinkActive } from '@angular/router';
import { Subscription } from 'rxjs';
import { LocalizedLicense, ScrapeResponse } from '../../graphql/types';
import {
  AppConfigService,
  VerticalSpecWithRoutes,
} from '../../services/app-config.service';
import { ServerConfigService } from '../../services/server-config.service';
import { dateFormat } from '../../services/session.service';
import { LicenseService } from '../../services/license.service';
import { GqlVertical } from '../../../generated/graphql';
import { addIcons } from 'ionicons';
import { logoGithub } from 'ionicons/icons';
import { NgIf } from '@angular/common';
import {
  IonHeader,
  IonToolbar,
  IonButtons,
  IonMenuButton,
  IonButton,
  IonIcon,
  IonContent,
  IonRouterOutlet,
} from '@ionic/angular/standalone';
import { TrialWarningComponent } from '../../components/trial-warning/trial-warning.component';
import { RepositoriesButtonComponent } from '../../components/repositories-button/repositories-button.component';
import { AgentsButtonComponent } from '../../components/agents-button/agents-button.component';
import { NotificationsButtonComponent } from '../../components/notifications-button/notifications-button.component';
import { DarkModeButtonComponent } from '../../components/dark-mode-button/dark-mode-button.component';
import { LoginButtonComponent } from '../../components/login-button/login-button.component';

@Component({
  selector: 'app-rss-builder-product-page',
  templateUrl: './rss-builder-product.page.html',
  styleUrls: ['./rss-builder-product.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    NgIf,
    IonHeader,
    TrialWarningComponent,
    IonToolbar,
    IonButtons,
    IonMenuButton,
    RouterLink,
    RepositoriesButtonComponent,
    AgentsButtonComponent,
    IonButton,
    IonIcon,
    RouterLinkActive,
    NotificationsButtonComponent,
    DarkModeButtonComponent,
    LoginButtonComponent,
    IonContent,
    IonRouterOutlet,
  ],
  standalone: true,
})
export class RssBuilderProductPage implements OnInit, OnDestroy {
  scrapeResponse: ScrapeResponse;
  productConfig: VerticalSpecWithRoutes;
  url: string;
  private subscriptions: Subscription[] = [];
  license: LocalizedLicense;

  protected readonly dateFormat = dateFormat;
  protected embedded: boolean = false;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly appConfigService: AppConfigService,
    private readonly licenseService: LicenseService,
    readonly serverConfig: ServerConfigService,
    private readonly changeRef: ChangeDetectorRef,
  ) {
    addIcons({ logoGithub });
  }

  async ngOnInit() {
    this.subscriptions.push(
      this.appConfigService
        .getActiveProductConfigChange()
        .subscribe((productConfig) => {
          this.productConfig = productConfig;
          this.changeRef.detectChanges();
        }),
      this.licenseService.licenseChange.subscribe((license) => {
        this.license = license;
        this.changeRef.detectChanges();
      }),
      this.activatedRoute.queryParams.subscribe((queryParams) => {
        if (queryParams.url) {
          this.url = queryParams.url;
          this.changeRef.detectChanges();
        }
        if (queryParams.embedded) {
          this.embedded = true;
          this.changeRef.detectChanges();
        }
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  protected readonly GqlProductName = GqlVertical;
}
