import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnInit,
} from '@angular/core';
import { AppConfigService } from '../../services/app-config.service';
import {
  IonAccordion,
  IonAccordionGroup,
  IonBreadcrumb,
  IonBreadcrumbs,
  IonCol,
  IonContent,
  IonItem,
  IonLabel,
  IonList,
  IonRow,
  IonText,
} from '@ionic/angular/standalone';
import { RouterLink } from '@angular/router';
import { Plan, PlanService } from '../../services/plan.service';
import { FeatureComponent } from '../../components/feature/feature.component';
import { DatePipe } from '@angular/common';
import { dateFormat } from '../../services/session.service';
import dayjs from 'dayjs';

@Component({
  selector: 'app-subscriptions-page',
  templateUrl: './subscriptions.page.html',
  styleUrls: ['./subscriptions.page.scss'],
  imports: [
    IonContent,
    IonBreadcrumbs,
    IonBreadcrumb,
    RouterLink,
    IonRow,
    IonCol,
    IonList,
    IonItem,
    IonLabel,
    IonAccordionGroup,
    IonAccordion,
    FeatureComponent,
    DatePipe,
    IonText,
  ],
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SubscriptionsPage implements OnInit {
  private readonly appConfigService = inject(AppConfigService);
  private readonly planService = inject(PlanService);
  private readonly changeRef = inject(ChangeDetectorRef);
  protected plans: Plan[] = [];
  protected readonly dateFormat = dateFormat;

  constructor() {
    this.appConfigService.setPageTitle('Plugins');
  }

  async ngOnInit() {
    this.plans = await this.planService.fetchPlans({ page: 0 });
    // await this.productService.listProducts({
    //   id: { in: session.user.plans.map(it => it.productId) },
    // });
    this.changeRef.detectChanges();
  }

  toDate(date: number) {
    return new Date(date);
  }

  getNextPaymentDate(plan: Plan) {
    return dayjs(plan.startedAt).add(1, 'years').toDate();
  }
}
