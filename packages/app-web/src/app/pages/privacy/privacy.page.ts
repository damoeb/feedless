import { Component } from '@angular/core';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-privacy',
  templateUrl: './privacy.page.html',
  styleUrls: ['./privacy.page.scss'],
})
export class PrivacyPage {
  constructor(titleService: Title) {
    titleService.setTitle('Privacy');
  }
}
