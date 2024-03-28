import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { ProductTeaser, TeaserProductsService } from '../services/teaser-products.service';

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
    private readonly teaserProducts: TeaserProductsService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    this.subscriptions.push(
    this.activatedRoute.params.subscribe(params => {
      this.product = this.teaserProducts.getProducts().find(p => p.id === params.productId)
    }));
    this.changeRef.detectChanges();
  }


  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
