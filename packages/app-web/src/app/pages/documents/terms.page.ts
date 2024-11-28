import { Component, inject } from '@angular/core';
import { AppConfigService } from '../../services/app-config.service';
import { IonContent } from '@ionic/angular/standalone';

@Component({
  selector: 'app-terms-page',
  templateUrl: './terms.page.html',
  imports: [IonContent],
  standalone: true,
})
export class TermsPage {
  constructor() {
    const appConfig = inject(AppConfigService);

    appConfig.setPageTitle('Terms');
  }
}
