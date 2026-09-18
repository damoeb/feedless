import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { AppConfigService } from '../../services/app-config.service';
import { IonContent } from '@ionic/angular/standalone';

@Component({
  selector: 'app-contact',
  templateUrl: './contact.page.html',
  imports: [IonContent],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: true,
})
export class ContactPage {
  constructor() {
    const appConfig = inject(AppConfigService);

    appConfig.setPageTitle('Contact');
  }
}
