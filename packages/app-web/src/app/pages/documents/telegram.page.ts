import { Component } from '@angular/core';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-setup-telegram-page',
  templateUrl: './telegram.page.html',
})
export class TelegramPage {
  constructor(titleService: Title) {
    titleService.setTitle('Telegram Setup');
  }
}
