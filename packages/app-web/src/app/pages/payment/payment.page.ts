import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { ServerConfigService } from '../../services/server-config.service';
import { AppConfigService } from '../../services/app-config.service';
import { IonContent, IonSpinner } from '@ionic/angular/standalone';

@Component({
  selector: 'app-payment-page',
  templateUrl: './payment.page.html',
  styleUrls: ['./payment.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonContent, IonSpinner],
  standalone: true,
})
export class PaymentPage implements OnInit, OnDestroy {
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly serverConfig = inject(ServerConfigService);
  private readonly appConfig = inject(AppConfigService);
  private readonly changeRef = inject(ChangeDetectorRef);

  private subscriptions: Subscription[] = [];

  async ngOnInit() {
    this.appConfig.setPageTitle('Payment');
    this.subscriptions.push(
      this.activatedRoute.params.subscribe(async (params) => {
        if (params.billingId) {
          // setTimeout(() => {
          //   location.href = `${this.serverConfig.apiUrl}/payment/${params.billingId}/callback`;
          // }, 3000);
        }
      })
    );
    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
