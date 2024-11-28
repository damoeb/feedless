import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WorkflowBuilderPageRoutingModule } from './workflow-builder-routing.module';
import { WorkflowBuilderPage } from './workflow-builder.page';




import { IonContent } from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    WorkflowBuilderPageRoutingModule,
    IonContent,
    WorkflowBuilderPage,
],
})
export class WorkflowBuilderPageModule {}
