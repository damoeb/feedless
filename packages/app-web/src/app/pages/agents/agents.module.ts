import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AgentsPageRoutingModule } from './agents-routing.module';

import { AgentsPage } from './agents.page';


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
    IonContent,
    IonBreadcrumbs,
    IonBreadcrumb,
    IonRow,
    IonCol,
    AgentsPage,
],
})
export class AgentsPageModule {}
