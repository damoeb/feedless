import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { ServerConfigService } from '../../services/server-config.service';
import { AppConfigService } from '../../services/app-config.service';

@Component({
    selector: 'app-payment-page',
    templateUrl: './payment.page.html',
    styleUrls: ['./payment.page.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class PaymentPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly serverConfig: ServerConfigService,
    private readonly appConfig: AppConfigService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    this.appConfig.setPageTitle('Payment');
    this.subscriptions.push(
      this.activatedRoute.params.subscribe(async (params) => {
        if (params.billingId) {
          setTimeout(() => {
            location.href = `${this.serverConfig.apiUrl}/payment/${params.billingId}/callback`;
          }, 3000);
        }
      }),
    );
    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
