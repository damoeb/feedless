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
  selector: 'app-applications-page',
  templateUrl: './applications.page.html',
  styleUrls: ['./applications.page.scss'],
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
export class ApplicationsPage {
  private readonly appConfigService = inject(AppConfigService);

  constructor() {
    this.appConfigService.setPageTitle('Applications');
  }
}
