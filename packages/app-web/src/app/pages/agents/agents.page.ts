import { Component } from '@angular/core';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-agents-page',
  templateUrl: './agents.page.html',
  styleUrls: ['./agents.page.scss'],
})
export class AgentsPage {
  constructor(titleService: Title) {
    titleService.setTitle('Agents');
  }
}
