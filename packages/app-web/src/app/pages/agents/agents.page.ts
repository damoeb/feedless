import { Component } from '@angular/core';
import { AppConfigService } from '../../services/app-config.service';
import { FeedlessHeaderComponent } from '../../components/feedless-header/feedless-header.component';
import {
  IonContent,
  IonBreadcrumbs,
  IonBreadcrumb,
  IonRow,
  IonCol,
} from '@ionic/angular/standalone';
import { RouterLink } from '@angular/router';
import { AgentsComponent } from '../../components/agents/agents.component';

@Component({
  selector: 'app-agents-page',
  templateUrl: './agents.page.html',
  styleUrls: ['./agents.page.scss'],
  imports: [
    FeedlessHeaderComponent,
    IonContent,
    IonBreadcrumbs,
    IonBreadcrumb,
    RouterLink,
    IonRow,
    IonCol,
    AgentsComponent,
  ],
  standalone: true,
})
export class AgentsPage {
  constructor(appConfigService: AppConfigService) {
    appConfigService.setPageTitle('Agents');
  }
}
