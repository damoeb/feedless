import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnInit,
} from '@angular/core';
import { relativeTimeOrElse } from '../../components/agents/agents.component';
import { OrderService } from '../../services/order.service';
import { Order } from '../../types';
import { AppConfigService } from '../../services/app-config.service';
import { FeedlessHeaderComponent } from '../../components/feedless-header/feedless-header.component';
import {
  IonCol,
  IonContent,
  IonItem,
  IonLabel,
  IonList,
  IonRow,
} from '@ionic/angular/standalone';

@Component({
  selector: 'app-billings-page',
  templateUrl: './billings.page.html',
  styleUrls: ['./billings.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FeedlessHeaderComponent,
    IonContent,
    IonRow,
    IonCol,
    IonList,
    IonItem,
    IonLabel,
  ],
  standalone: true,
})
export class BillingsPage implements OnInit {
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly appConfig = inject(AppConfigService);
  private readonly orderService = inject(OrderService);

  busy = false;
  orders: Order[] = [];
  fromNow = relativeTimeOrElse;

  async ngOnInit() {
    this.appConfig.setPageTitle('Billings');
    await this.fetchOrders();
  }

  private async fetchOrders() {
    const page = 0;
    const orders = await this.orderService.orders({
      cursor: {
        page,
      },
    });
    this.orders.push(...orders);
    this.changeRef.detectChanges();
  }
}
