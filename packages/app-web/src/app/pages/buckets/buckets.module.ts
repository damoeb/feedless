import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { BucketsPageRoutingModule } from './buckets-routing.module';

import { BucketsPage } from './buckets.page';
import { BubbleModule } from '../../components/bubble/bubble.module';
import { FilterToolbarModule } from '../../components/filter-toolbar/filter-toolbar.module';
import { PageHeaderModule } from '../../components/page-header/page-header.module';
import { FeatureToggleModule } from '../../directives/feature-toggle/feature-toggle.module';
import { ImportModalModule } from '../../modals/import-modal/import-modal.module';
import { ExportModalModule } from '../../modals/export-modal/export-modal.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    BucketsPageRoutingModule,
    BubbleModule,
    FilterToolbarModule,
    PageHeaderModule,
    FeatureToggleModule,
    ReactiveFormsModule,
    ImportModalModule,
    ExportModalModule,
  ],
  declarations: [BucketsPage],
})
export class BucketsPageModule {}
