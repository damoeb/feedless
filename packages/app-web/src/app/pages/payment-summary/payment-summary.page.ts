import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { OrderService } from '../../services/order.service';
import { Order } from '../../types';

@Component({
  selector: 'app-payment-summary-page',
  templateUrl: './payment-summary.page.html',
  styleUrls: ['./payment-summary.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaymentSummaryPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];
  private billingId: string;
  protected order: Order;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly orderService: OrderService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    this.subscriptions.push(
      this.activatedRoute.params.subscribe(async (params) => {
        if (params.billingId) {
          this.billingId = params.billingId;
          this.order = await this.orderService.orders({
            cursor: {
              page: 0,
              pageSize: 1
            },
            where: {
              id: {
                equals: this.billingId
              }
            }
          }).then(billings => billings[0]);
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
