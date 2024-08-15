import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { relativeTimeOrElse } from '../../components/agents/agents.component';
import { OrderService } from '../../services/order.service';
import { Order } from '../../types';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-billings-page',
  templateUrl: './billings.page.html',
  styleUrls: ['./billings.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BillingsPage implements OnInit {
  busy = false;
  orders: Order[] = [];

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly titleService: Title,
    private readonly orderService: OrderService,
  ) {}

  async ngOnInit() {
    this.titleService.setTitle('Billings');
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

  fromNow = relativeTimeOrElse;
}
