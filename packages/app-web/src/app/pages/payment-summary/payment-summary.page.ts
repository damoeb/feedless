import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { BillingService } from '../../services/billing.service';
import { Billing } from '../../types';

@Component({
  selector: 'app-payment-summary-page',
  templateUrl: './payment-summary.page.html',
  styleUrls: ['./payment-summary.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaymentSummaryPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];
  private billingId: string;
  protected billing: Billing;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly billingService: BillingService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    this.subscriptions.push(
      this.activatedRoute.params.subscribe(async (params) => {
        if (params.billingId) {
          this.billingId = params.billingId;
          this.billing = await this.billingService.billings({
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
