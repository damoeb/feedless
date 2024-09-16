import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';

import { NotebookBuilderPageRoutingModule } from './notebook-builder-routing.module';
import { NotebookBuilderPage } from './notebook-builder.page';
import { NotebookBuilderModule } from '../../components/notebook-builder/notebook-builder.module';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    NotebookBuilderPageRoutingModule,
    NotebookBuilderModule,
    FeedlessHeaderModule,
  ],
  declarations: [NotebookBuilderPage],
})
export class NotebookBuilderPageModule {}
