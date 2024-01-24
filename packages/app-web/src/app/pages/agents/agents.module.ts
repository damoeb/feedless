import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { AgentsPage } from './agents.page';
import { BubbleModule } from '../../components/bubble/bubble.module';
import { FeatureToggleModule } from '../../directives/feature-toggle/feature-toggle.module';
import { AgentsRoutingModule } from './agents-routing.module';
import { AgentsModule } from '../../components/agents/agents.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    BubbleModule,
    AgentsRoutingModule,
    FeatureToggleModule,
    ReactiveFormsModule,
    AgentsModule
  ],
  declarations: [AgentsPage]
})
export class AgentsPageModule {
}
