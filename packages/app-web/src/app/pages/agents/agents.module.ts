import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { AgentsPageRoutingModule } from './agents-routing.module';

import { AgentsPage } from './agents.page';
import { PlansModule } from '../../components/plans/plans.module';
import { AgentsModule } from '../../components/agents/agents.module';
import { OpmlPickerModule } from '../../components/opml-picker/opml-picker.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    AgentsPageRoutingModule,
    PlansModule,
    AgentsModule,
    OpmlPickerModule,
  ],
  declarations: [AgentsPage],
})
export class AgentsPageModule {}
