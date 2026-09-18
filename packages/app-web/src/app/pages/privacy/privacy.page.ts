import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { AppConfigService } from '../../services/app-config.service';
import { IonContent } from '@ionic/angular/standalone';

@Component({
  selector: 'app-privacy',
  templateUrl: './privacy.page.html',
  styleUrls: ['./privacy.page.scss'],
  imports: [IonContent],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: true,
})
export class PrivacyPage {
  constructor() {
    const appConfig = inject(AppConfigService);

    appConfig.setPageTitle('Privacy');
  }
}
