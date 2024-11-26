import { Component } from '@angular/core';
import { AppConfigService } from '../../services/app-config.service';

@Component({
  selector: 'app-setup-telegram-page',
  templateUrl: './telegram.page.html',
  standalone: false,
})
export class TelegramPage {
  constructor(appConfig: AppConfigService) {
    appConfig.setPageTitle('Telegram Setup');
  }
}
