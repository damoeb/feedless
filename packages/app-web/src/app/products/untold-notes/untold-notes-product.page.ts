import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { ScrapeResponse } from '../../graphql/types';
import {
  ProductConfig,
  AppConfigService,
} from '../../services/app-config.service';

@Component({
  selector: 'app-untold-notes-product-page',
  templateUrl: './untold-notes-product.page.html',
  styleUrls: ['./untold-notes-product.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntoldNotesProductPage implements OnInit, OnDestroy {
  scrapeResponse: ScrapeResponse;
  productConfig: ProductConfig;
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly appConfigService: AppConfigService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    this.subscriptions.push(
      this.appConfigService
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
