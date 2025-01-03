import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { AppConfigService } from '../../services/app-config.service';
import { FeedlessHeaderComponent } from '../../components/feedless-header/feedless-header.component';
import {
  IonBreadcrumb,
  IonBreadcrumbs, IonButton,
  IonCol,
  IonContent,
  IonItem,
  IonLabel,
  IonList,
  IonRow
} from '@ionic/angular/standalone';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-services-page',
  templateUrl: './services.page.html',
  styleUrls: ['./services.page.scss'],
  imports: [
    FeedlessHeaderComponent,
    IonContent,
    IonBreadcrumbs,
    IonBreadcrumb,
    RouterLink,
    IonRow,
    IonCol,
    IonList,
    IonItem,
    IonLabel,
    IonButton,
  ],
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ServicesPage {
  private readonly appConfigService = inject(AppConfigService);

  constructor() {
    this.appConfigService.setPageTitle('Connected Services');
  }
}
