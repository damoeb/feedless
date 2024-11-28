import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { OrderService } from '../../services/order.service';
import { Order } from '../../types';
import { AppConfigService } from '../../services/app-config.service';
import { FeedlessHeaderComponent } from '../../components/feedless-header/feedless-header.component';
import { IonContent } from '@ionic/angular/standalone';
import { JsonPipe } from '@angular/common';

@Component({
  selector: 'app-payment-summary-page',
  templateUrl: './payment-summary.page.html',
  styleUrls: ['./payment-summary.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FeedlessHeaderComponent, IonContent, JsonPipe],
  standalone: true,
})
export class PaymentSummaryPage implements OnInit, OnDestroy {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly orderService = inject(OrderService);
  private readonly appConfig = inject(AppConfigService);
  private readonly changeRef = inject(ChangeDetectorRef);

  protected order: Order;
  private subscriptions: Subscription[] = [];
  private billingId: string;

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
