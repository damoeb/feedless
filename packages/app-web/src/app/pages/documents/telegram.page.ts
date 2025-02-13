import { Component, inject } from '@angular/core';
import { AppConfigService } from '../../services/app-config.service';
import { IonContent } from '@ionic/angular/standalone';

@Component({
  selector: 'app-setup-telegram-page',
  templateUrl: './telegram.page.html',
  imports: [IonContent],
  standalone: true,
})
export class TelegramPage {
  constructor() {
    const appConfig = inject(AppConfigService);

    appConfig.setPageTitle('Telegram Setup');
  }
}
