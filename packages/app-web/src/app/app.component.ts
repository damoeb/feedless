import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from './services/auth.service';
import { SessionService } from './services/session.service';
import { Subscription } from 'rxjs';
import {
  AppConfigService,
  VerticalSpecWithRoutes,
} from './services/app-config.service';
import { GqlVertical } from '../generated/graphql';
import { kebabCase } from 'lodash-es';
import {
  IonApp,
  IonButtons,
  IonContent,
  IonFooter,
  IonHeader,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonMenu,
  IonMenuButton,
  IonRouterOutlet,
  IonSplitPane,
  IonToolbar,
  MenuController,
} from '@ionic/angular/standalone';
import { ProductTitleComponent } from './components/product-title/product-title.component';
import { ServerConfigService } from './services/server-config.service';
import { addIcons } from 'ionicons';
import {
  cardOutline,
  exitOutline,
  menuOutline,
  personOutline,
  settingsOutline,
} from 'ionicons/icons';
import dayjs from 'dayjs';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonApp,
    IonRouterOutlet,
    IonSplitPane,
    IonMenu,
    IonHeader,
    IonToolbar,
    IonButtons,
    IonMenuButton,
    ProductTitleComponent,
    IonContent,
    IonFooter,
    IonIcon,
    IonList,
    IonItem,
    RouterLink,
    IonLabel,
  ],
  standalone: true,
})
export class AppComponent implements OnDestroy, OnInit {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly appConfigService = inject(AppConfigService);
  protected readonly sessionService = inject(SessionService);
  private readonly authService = inject(AuthService);
  private readonly menuController = inject(MenuController);
  protected readonly serverConfig = inject(ServerConfigService);

  verticalConfig!: VerticalSpecWithRoutes;
  private subscriptions: Subscription[] = [];
  private isDarkMode!: boolean;
  private vertical!: GqlVertical;

  constructor() {
    addIcons({
      menuOutline,
      personOutline,
      settingsOutline,
      cardOutline,
      exitOutline,
    });
  }

  ngOnInit(): void {
    this.subscriptions.push(
      this.appConfigService
        .getActiveProductConfigChange()
        .subscribe((verticalConfig) => {
          this.verticalConfig = verticalConfig;
          this.changeRef.detectChanges();
        }),
      this.activatedRoute.queryParams.subscribe(async (queryParams) => {
        if (queryParams.token) {
          console.log('with token');
          await this.authService.handleAuthenticationToken(queryParams.token);
          await this.router.navigate([], {
            queryParams: {
              signup: null,
              token: null,
            },
            queryParamsHandling: 'merge',
          });
        } else {
          console.log('without token');
          await this.sessionService.fetchSession();
        }
      }),
      this.sessionService.watchColorScheme().subscribe((isDarkMode) => {
        this.isDarkMode = isDarkMode;
        this.propagateColorModeAndProduct();
      }),
      this.appConfigService
        .getActiveProductConfigChange()
        .subscribe((vertical) => {
          this.vertical = vertical.product;
          this.propagateColorModeAndProduct();
        }),
    );
  }

  getBuildCommit(): string {
    return this.serverConfig.getBuild().commit.substring(0, 6);
  }

  getBuildDate(): string {
    return dayjs(this.serverConfig.getBuild().date).format('DD-MM-YYYY');
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  // getBreakpointMinWidth(): SidemenuBreakpoint {
  //   return this.verticalConfig.sideMenu?.breakpoint ?? 'sm';
  // }

  private propagateColorModeAndProduct() {
    const classNames: string[] = [];
    if (this.isDarkMode) {
      classNames.push('dark');
    } else {
      classNames.push('light');
    }

    if (this.vertical) {
      classNames.push('product--' + kebabCase(this.vertical.toString()));
    }

    document.body.className = classNames.join(' ');
  }

  async toggleMenu() {
    if (await this.menuController.isOpen()) {
      console.log('close');
      await this.menuController.close();
    }
  }

  async logout() {
    await this.sessionService.logout();
    location.reload();
  }
}
