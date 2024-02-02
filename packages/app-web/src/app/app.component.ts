import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from './services/auth.service';
import { ProfileService } from './services/profile.service';
import { Subscription } from 'rxjs';
import { environment } from '../environments/environment';
import {
  ProductConfig,
  ProductService,
  SidemenuBreakpoint,
} from './services/product.service';
import { GqlProductName } from '../generated/graphql';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppComponent implements OnDestroy, OnInit {
  productConfig: ProductConfig;
  protected readonly GqlProductName = GqlProductName;
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly router: Router,
    private readonly changeRef: ChangeDetectorRef,
    private readonly productService: ProductService,
    private readonly profileService: ProfileService,
    private readonly authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.subscriptions.push(
      this.productService
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
          await new Promise((resolve) => setTimeout(resolve, 200));
          await this.profileService.fetchProfile('network-only');
        }
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  isActiveProduct(product: GqlProductName): boolean {
    return environment.product() === product;
  }

  getBreakpointMinWidth(): SidemenuBreakpoint {
    return this.productConfig.sideMenu?.breakpoint ?? 'sm';
  }
}
