import { Component } from '@angular/core';
import { AppConfigService } from '../../services/app-config.service';

@Component({
    selector: 'app-terms-page',
    templateUrl: './terms.page.html',
    standalone: false
})
export class TermsPage {
  constructor(appConfig: AppConfigService) {
    appConfig.setPageTitle('Terms');
  }
}
