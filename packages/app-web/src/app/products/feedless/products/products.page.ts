import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit, ViewEncapsulation } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { ProductService, ProductTeaser } from '../../../services/product.service';

@Component({
  selector: 'app-feedless-products-page',
  templateUrl: './products.page.html',
  styleUrls: ['./products.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None
})
export class ProductsPage implements OnInit, OnDestroy {

  private subscriptions: Subscription[] = [];
  product: ProductTeaser;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly productService: ProductService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    this.subscriptions.push(
    this.activatedRoute.params.subscribe(async params => {
      this.product = (await this.productService.getProductConfigs()).find(p => p.meta.id === params.productId).meta
      this.changeRef.detectChanges();
    }));
  }


  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  getImageUrl() {
    return `url("${this.product?.imageUrl}")`
  }
}
