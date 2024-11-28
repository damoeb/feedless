import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from './services/auth.service';
import { SessionService } from './services/session.service';
import { Subscription } from 'rxjs';
import {
  AppConfigService,
  VerticalSpecWithRoutes,
  SidemenuBreakpoint,
} from './services/app-config.service';
import { GqlVertical } from '../generated/graphql';
import { kebabCase } from 'lodash-es';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class AppComponent implements OnDestroy, OnInit {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly appConfigService = inject(AppConfigService);
  private readonly sessionService = inject(SessionService);
  private readonly authService = inject(AuthService);

  verticalConfig!: VerticalSpecWithRoutes;
  private subscriptions: Subscription[] = [];
  private isDarkMode!: boolean;
  private vertical!: GqlVertical;

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
}
