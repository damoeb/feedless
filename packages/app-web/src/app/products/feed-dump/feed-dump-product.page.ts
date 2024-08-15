import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import {
  AppConfigService,
  ProductConfig,
} from '../../services/app-config.service';
import { ServerConfigService } from '../../services/server-config.service';
import { Authentication, AuthService } from '../../services/auth.service';
import { ModalService } from '../../services/modal.service';

@Component({
  selector: 'app-feed-dump-product-page',
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
    private readonly appConfigService: AppConfigService,
    private readonly modalService: ModalService,
    private readonly authService: AuthService,
    readonly serverConfig: ServerConfigService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    this.subscriptions.push(
      this.authService
        .authorizationChange()
        .subscribe(async (authorization) => {
          this.authorization = authorization;
          this.changeRef.detectChanges();
        }),
      this.appConfigService
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
