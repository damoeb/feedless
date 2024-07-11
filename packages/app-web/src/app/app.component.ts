import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from './services/auth.service';
import { SessionService } from './services/session.service';
import { Subscription } from 'rxjs';
import {
  ProductConfig,
  AppConfigService,
  SidemenuBreakpoint,
} from './services/app-config.service';
import { GqlProductCategory } from '../generated/graphql';
import { kebabCase } from 'lodash-es';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppComponent implements OnDestroy, OnInit {
  productConfig: ProductConfig;
  protected readonly GqlProductName = GqlProductCategory;
  private subscriptions: Subscription[] = [];
  private isDarkMode: boolean;
  private product: GqlProductCategory;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly router: Router,
    private readonly changeRef: ChangeDetectorRef,
    private readonly appConfigService: AppConfigService,
    private readonly sessionService: SessionService,
    private readonly authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.subscriptions.push(
      this.appConfigService
        .getActiveProductConfigChange()
        .subscribe((productConfig) => {
          this.productConfig = productConfig;
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
        .subscribe((product) => {
          this.product = product.product;
          this.propagateColorModeAndProduct();
        }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  getBreakpointMinWidth(): SidemenuBreakpoint {
    return this.productConfig.sideMenu?.breakpoint ?? 'sm';
  }

  private propagateColorModeAndProduct() {
    const classNames: string[] = [];
    if (this.isDarkMode) {
      classNames.push('dark');
    } else {
      classNames.push('light');
    }

    if (this.product) {
      classNames.push('product--' + kebabCase(this.product.toString()));
    }

    document.body.className = classNames.join(' ');
  }
}
