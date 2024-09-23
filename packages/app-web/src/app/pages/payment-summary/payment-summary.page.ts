import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { OrderService } from '../../services/order.service';
import { Order } from '../../types';
import { AppConfigService } from '../../services/app-config.service';

@Component({
  selector: 'app-payment-summary-page',
  templateUrl: './payment-summary.page.html',
  styleUrls: ['./payment-summary.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaymentSummaryPage implements OnInit, OnDestroy {
  protected order: Order;
  private subscriptions: Subscription[] = [];
  private billingId: string;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly orderService: OrderService,
    private readonly appConfig: AppConfigService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    this.appConfig.setPageTitle('Payment Summary');
    this.subscriptions.push(
      this.activatedRoute.params.subscribe(async (params) => {
        if (params.billingId) {
          this.billingId = params.billingId;
          this.order = await this.orderService
            .orders({
              cursor: {
                page: 0,
                pageSize: 1,
              },
              where: {
                id: {
                  eq: this.billingId,
                },
              },
            })
            .then((billings) => billings[0]);
          this.changeRef.detectChanges();
        }
      }),
    );
    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
