import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { ProductService, ProductTeaser } from '../../../services/product.service';

@Component({
  selector: 'app-buy-page',
  templateUrl: './buy.page.html',
  styleUrls: ['./buy.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BuyPage implements OnInit, OnDestroy {

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
    }));
    this.changeRef.detectChanges();
  }


  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
