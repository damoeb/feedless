import { Component } from '@angular/core';
import { AppConfigService } from '../../services/app-config.service';
import { IonRouterLink } from '@ionic/angular/standalone';

@Component({
  selector: 'app-agents-page',
  templateUrl: './agents.page.html',
  styleUrls: ['./agents.page.scss'],
})
export class AgentsPage {
  constructor(appConfigService: AppConfigService) {
    appConfigService.setPageTitle('Agents');
  }
}
