import { Component } from '@angular/core';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-terms-page',
  templateUrl: './terms.page.html',
})
export class TermsPage {
  constructor(titleService: Title) {
    titleService.setTitle('Terms');
  }
}
