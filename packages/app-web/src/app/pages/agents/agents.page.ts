import { Component, inject } from '@angular/core';
import { AppConfigService } from '../../services/app-config.service';
import {
  IonBreadcrumb,
  IonBreadcrumbs,
  IonCol,
  IonContent,
  IonRow,
} from '@ionic/angular/standalone';
import { RouterLink } from '@angular/router';
import { AgentsComponent } from '../../components/agents/agents.component';

@Component({
  selector: 'app-agents-page',
  templateUrl: './agents.page.html',
  styleUrls: ['./agents.page.scss'],
  imports: [
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
  constructor() {
    const appConfigService = inject(AppConfigService);

    appConfigService.setPageTitle('Agents');
  }
}
