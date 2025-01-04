import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { AppConfigService } from '../../services/app-config.service';
import {
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

@Component({
  selector: 'app-plugins-page',
  templateUrl: './plugins.page.html',
  styleUrls: ['./plugins.page.scss'],
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
  ],
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PluginsPage {
  private readonly appConfigService = inject(AppConfigService);

  constructor() {
    this.appConfigService.setPageTitle('Plugins');
  }
}
