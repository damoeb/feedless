import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from './services/auth.service';
import { ProfileService } from './services/profile.service';
import { Subscription } from 'rxjs';
import { environment } from '../environments/environment';
import { ProductConfig, ProductService } from './services/product.service';
import { GqlProduct } from '../generated/graphql';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss'],
})
export class AppComponent implements OnDestroy, OnInit {
  public metaPages = [
    { title: 'About', url: '/about' },
    // { title: 'Privacy', url: '/privacy' },
    // { title: 'Terms', url: '/terms' },
    // { title: 'Contact', url: '/contact' },
  ];

  private subscriptions: Subscription[] = [];
  productConfig: ProductConfig;
  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly router: Router,
    private readonly product: ProductService,
    private readonly profileService: ProfileService,
    private readonly authService: AuthService,
  ) {
    this.subscriptions.push(
      activatedRoute.queryParams.subscribe(async (queryParams) => {
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
          await profileService.fetchProfile('network-only');
        }
      }),
    );
  }

  ngOnInit(): void {
    this.productConfig = this.product.getProductConfig();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  isActiveProduct(product: GqlProduct): boolean {
    return environment.product() === product;
  }

  protected readonly GqlProduct = GqlProduct;
}
