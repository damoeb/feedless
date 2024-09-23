import { Component } from '@angular/core';
import { AppConfigService } from '../../services/app-config.service';

@Component({
  selector: 'app-contact',
  templateUrl: './contact.page.html',
  styleUrls: ['./contact.page.scss'],
})
export class ContactPage {
  constructor(appConfig: AppConfigService) {
    appConfig.setPageTitle('Contact');
  }
}
