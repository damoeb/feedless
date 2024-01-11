import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { SourcesPageRoutingModule } from './sources-routing.module';

import { SourcesPage } from './sources.page';
import { BubbleModule } from '../../components/bubble/bubble.module';
import { PageHeaderModule } from '../../components/page-header/page-header.module';
import { FeatureToggleModule } from '../../directives/feature-toggle/feature-toggle.module';
import { ExportModalModule } from '../../modals/export-modal/export-modal.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    SourcesPageRoutingModule,
    BubbleModule,
    PageHeaderModule,
    FeatureToggleModule,
    ReactiveFormsModule,
    ExportModalModule,
  ],
  declarations: [SourcesPage],
})
export class SourcesPageModule {}
