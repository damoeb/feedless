import { Component } from '@angular/core';
import { AppConfigService } from '../../services/app-config.service';

@Component({
  selector: 'app-privacy',
  templateUrl: './privacy.page.html',
  styleUrls: ['./privacy.page.scss'],
})
export class PrivacyPage {
  constructor(appConfig: AppConfigService) {
    appConfig.setPageTitle('Privacy');
  }
}
