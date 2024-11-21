import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AgentsPageRoutingModule } from './agents-routing.module';

import { AgentsPage } from './agents.page';
import { AgentsModule } from '../../components/agents/agents.module';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';
import {
  IonBreadcrumb,
  IonBreadcrumbs,
  IonCol,
  IonContent,
  IonRow,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    AgentsPageRoutingModule,
    AgentsModule,
    FeedlessHeaderModule,
    IonContent,
    IonBreadcrumbs,
    IonBreadcrumb,
    IonRow,
    IonCol,
  ],
  declarations: [AgentsPage],
})
export class AgentsPageModule {}
