import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { relativeTimeOrElse } from '../../components/agents/agents.component';
import { OrderService } from '../../services/order.service';
import { Order } from '../../types';
import { AppConfigService } from '../../services/app-config.service';
import { FeedlessHeaderComponent } from '../../components/feedless-header/feedless-header.component';
import {
  IonContent,
  IonRow,
  IonCol,
  IonList,
  IonItem,
  IonLabel,
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
    IonLabel
],
  standalone: true,
})
export class BillingsPage implements OnInit {
  busy = false;
  orders: Order[] = [];
  fromNow = relativeTimeOrElse;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly appConfig: AppConfigService,
    private readonly orderService: OrderService,
  ) {}

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
