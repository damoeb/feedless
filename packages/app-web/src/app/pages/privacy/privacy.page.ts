import { Component, inject } from '@angular/core';
import { AppConfigService } from '../../services/app-config.service';
import { FeedlessHeaderComponent } from '../../components/feedless-header/feedless-header.component';
import { IonContent } from '@ionic/angular/standalone';

@Component({
  selector: 'app-privacy',
  templateUrl: './privacy.page.html',
  styleUrls: ['./privacy.page.scss'],
  imports: [FeedlessHeaderComponent, IonContent],
  standalone: true,
})
export class PrivacyPage {
  constructor() {
    const appConfig = inject(AppConfigService);

    appConfig.setPageTitle('Privacy');
  }
}
