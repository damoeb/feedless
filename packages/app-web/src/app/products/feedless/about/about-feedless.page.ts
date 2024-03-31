import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import '@justinribeiro/lite-youtube';

import { fixUrl } from '../../../app.module';
import { ProductService, ProductTeaser } from '../../../services/product.service';

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
              readonly productService: ProductService) {}

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
    const configs = await this.productService.getProductConfigs();
    this.products = configs.map(c => c.meta);
    this.waitList = false;
    this.changeRef.detectChanges();
  }
}
