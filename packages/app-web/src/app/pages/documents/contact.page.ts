import { Component, inject } from '@angular/core';
import { AppConfigService } from '../../services/app-config.service';
import { IonContent } from '@ionic/angular/standalone';

@Component({
  selector: 'app-contact',
  templateUrl: './contact.page.html',
  imports: [IonContent],
  standalone: true,
})
export class ContactPage {
  constructor() {
    const appConfig = inject(AppConfigService);

    appConfig.setPageTitle('Contact');
  }
}
