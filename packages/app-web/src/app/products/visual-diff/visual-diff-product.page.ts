import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ProfileService } from '../../services/profile.service';
import { ChildActivationEnd, Router } from '@angular/router';
import { has } from 'lodash-es';
import { ProductConfig, ProductService } from '../../services/product.service';
import { filter, map, Subscription } from 'rxjs';

@Component({
  selector: 'app-visual-diff-product',
  templateUrl: './visual-diff-product.page.html',
  styleUrls: ['./visual-diff-product.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VisualDiffProductPage implements OnInit, OnDestroy {
  productConfig: ProductConfig;
  private subscriptions: Subscription[] = [];
  activePageTitle: string;

  constructor(
    readonly profile: ProfileService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly router: Router,
    private readonly productService: ProductService,
  ) {}

  ngOnInit() {
    this.subscriptions.push(
      this.productService
        .getActiveProductConfigChange()
        .subscribe((productConfig) => {
          this.productConfig = productConfig;
          this.changeRef.detectChanges();
        }),
      this.router.events
        .pipe(
          filter((e) => e instanceof ChildActivationEnd),
          map((e) => (e as ChildActivationEnd).snapshot.firstChild.data),
          filter((data) => has(data, 'title')),
        )
        .subscribe((data) => {
          this.activePageTitle = data.title;
          this.changeRef.detectChanges();
        }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
