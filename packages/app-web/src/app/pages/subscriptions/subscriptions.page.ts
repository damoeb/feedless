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
} from '@ionic/angular/standalone';
import { RouterLink } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { Plan, PlanService } from '../../services/plan.service';
import { FeatureComponent } from '../../components/feature/feature.component';

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
  ],
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SubscriptionsPage implements OnInit {
  private readonly appConfigService = inject(AppConfigService);
  private readonly productService = inject(ProductService);
  private readonly planService = inject(PlanService);
  private readonly changeRef = inject(ChangeDetectorRef);
  protected plans: Plan[] = [];

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
}
