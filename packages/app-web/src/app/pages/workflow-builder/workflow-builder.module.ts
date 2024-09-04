import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';

import { FeedBuilderPageRoutingModule } from './workflow-builder-routing.module';
import { WorkflowBuilderPage } from './workflow-builder.page';
import { FeedBuilderModule } from '../../components/feed-builder/feed-builder.module';
import { RepositoryModalModule } from '../../modals/repository-modal/repository-modal.module';
import { WorkflowBuilderModule } from '../../components/workflow-builder/workflow-builder.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    FeedBuilderPageRoutingModule,
    RepositoryModalModule,
    FeedBuilderModule,
    WorkflowBuilderModule,
  ],
  declarations: [WorkflowBuilderPage],
})
export class WorkflowBuilderPageModule {}
