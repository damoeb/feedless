import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ProductConfig, ProductService } from '../../services/product.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-product-title',
  templateUrl: './product-title.component.html',
  styleUrls: ['./product-title.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductTitleComponent implements OnInit, OnDestroy {
  productConfig: ProductConfig;
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly productService: ProductService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.subscriptions.push(
      this.productService
        .getActiveProductConfigChange()
        .subscribe((productConfig) => {
          this.productConfig = productConfig;
          this.changeRef.detectChanges();
        }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
