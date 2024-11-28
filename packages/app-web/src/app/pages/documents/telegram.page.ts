import { Component } from '@angular/core';
import { AppConfigService } from '../../services/app-config.service';
import { IonContent } from '@ionic/angular/standalone';

@Component({
  selector: 'app-setup-telegram-page',
  templateUrl: './telegram.page.html',
  imports: [IonContent],
  standalone: true,
})
export class TelegramPage {
  constructor(appConfig: AppConfigService) {
    appConfig.setPageTitle('Telegram Setup');
  }
}
