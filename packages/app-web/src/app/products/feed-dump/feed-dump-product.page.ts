import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { ProductConfig, ProductService } from '../../services/product.service';
import { ServerSettingsService } from '../../services/server-settings.service';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import { Authentication, AuthService } from '../../services/auth.service';
import { ModalService } from '../../services/modal.service';

@Component({
  selector: 'app-team-draft-product-page',
  templateUrl: './feed-dump-product.page.html',
  styleUrls: ['./feed-dump-product.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedDumpProductPage implements OnInit, OnDestroy {
  productConfig: ProductConfig;
  url: string;
  private subscriptions: Subscription[] = [];
  protected authorization: Authentication;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly productService: ProductService,
    private readonly modalService: ModalService,
    private readonly authService: AuthService,
    readonly serverSettings: ServerSettingsService,
    private readonly changeRef: ChangeDetectorRef,
  ) {
    dayjs.extend(relativeTime);
  }

  async ngOnInit() {
    this.subscriptions.push(
      this.authService
        .authorizationChange()
        .subscribe(async (authorization) => {
          this.authorization = authorization;
          this.changeRef.detectChanges();
        }),
      this.productService
        .getActiveProductConfigChange()
        .subscribe((productConfig) => {
          this.productConfig = productConfig;
          this.changeRef.detectChanges();
        }),
      this.activatedRoute.queryParams.subscribe((queryParams) => {
        if (queryParams.url) {
          this.url = queryParams.url;
        }
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async openFeedBuilder() {
    await this.modalService.openFeedBuilder({});
  }
}
