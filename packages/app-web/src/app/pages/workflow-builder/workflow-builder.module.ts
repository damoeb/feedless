import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';

import { WorkflowBuilderPageRoutingModule } from './workflow-builder-routing.module';
import { WorkflowBuilderPage } from './workflow-builder.page';
import { FeedBuilderModule } from '../../components/feed-builder/feed-builder.module';
import { RepositoryModalModule } from '../../modals/repository-modal/repository-modal.module';
import { WorkflowBuilderModule } from '../../components/workflow-builder/workflow-builder.module';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    WorkflowBuilderPageRoutingModule,
    RepositoryModalModule,
    FeedBuilderModule,
    WorkflowBuilderModule,
    FeedlessHeaderModule,
  ],
  declarations: [WorkflowBuilderPage],
})
export class WorkflowBuilderPageModule {}
