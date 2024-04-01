import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { Router } from '@angular/router';
import '@justinribeiro/lite-youtube';

import { fixUrl } from '../../../app.module';
import {
  ProductService,
  ProductConfig,
} from '../../../services/product.service';

@Component({
  selector: 'app-about-feedless-page',
  templateUrl: './about-feedless.page.html',
  styleUrls: ['./about-feedless.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AboutFeedlessPage implements OnInit {
  waitList: boolean;
  stableProducts: ProductConfig[];
  unstableProducts: ProductConfig[];

  constructor(
    private readonly router: Router,
    private readonly changeRef: ChangeDetectorRef,
    readonly productService: ProductService,
  ) {}

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
    const allProducts = await this.productService.getProductConfigs();
    this.stableProducts = allProducts.filter((p) => !p.isUnstable);
    this.unstableProducts = allProducts.filter((p) => p.isUnstable);
    this.waitList = false;
    this.changeRef.detectChanges();
  }
}
