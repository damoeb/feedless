import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import '@justinribeiro/lite-youtube';

import { fixUrl } from '../../../app.module';
import { ProductTeaser, TeaserProductsService } from '../services/teaser-products.service';

@Component({
  selector: 'app-about-feedless-page',
  templateUrl: './about-feedless.page.html',
  styleUrls: ['./about-feedless.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AboutFeedlessPage implements OnInit {
  waitList: boolean;
  products: ProductTeaser[];

  constructor(private readonly router: Router,
              private readonly changeRef: ChangeDetectorRef,
              readonly teaserProducts: TeaserProductsService) {}

  async handleQuery(url: string) {
    try {
      await this.router.navigate(['/builder'], {
        queryParams: {
          url: fixUrl(url),
        },
      });
    } catch (e) {
      console.warn(e);
    }
  }

  async ngOnInit() {
    this.products = await this.teaserProducts.getProducts();
    this.waitList = false;
    this.changeRef.detectChanges();
  }
}
