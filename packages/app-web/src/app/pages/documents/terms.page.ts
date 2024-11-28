import { Component } from '@angular/core';
import { AppConfigService } from '../../services/app-config.service';
import { IonContent } from '@ionic/angular/standalone';

@Component({
  selector: 'app-terms-page',
  templateUrl: './terms.page.html',
  imports: [IonContent],
  standalone: true,
})
export class TermsPage {
  constructor(appConfig: AppConfigService) {
    appConfig.setPageTitle('Terms');
  }
}
