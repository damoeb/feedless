import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { Repository, WebDocument } from '../../graphql/types';
import { RepositoryService } from '../../services/repository.service';
import { BubbleColor } from '../../components/bubble/bubble.component';
import { GqlProductCategory, GqlVisibility } from '../../../generated/graphql';
import { relativeTimeOrElse } from '../../components/agents/agents.component';
import { BillingService } from '../../services/billing.service';
import { Billing } from '../../types';

@Component({
  selector: 'app-billings-page',
  templateUrl: './billings.page.html',
  styleUrls: ['./billings.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BillingsPage implements OnInit {
  busy = false;
  billings: Billing[] = [];

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly billingService: BillingService,
  ) {}

  async ngOnInit() {
    await this.fetchBillings();
  }

  private async fetchBillings() {
    const page = 0;
    const billings = await this.billingService.billings({
      cursor: {
        page,
      },
    });
    this.billings.push(...billings);
    this.changeRef.detectChanges();
  }

  fromNow = relativeTimeOrElse;
}
