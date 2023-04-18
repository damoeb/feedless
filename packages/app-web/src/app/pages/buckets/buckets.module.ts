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
import { BucketCreateModalModule } from '../../modals/bucket-create-modal/bucket-create-modal.module';
import { ImportModalModule } from '../../modals/import-modal/import-modal.module';

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
    BucketCreateModalModule,
    ReactiveFormsModule,
    ImportModalModule,
  ],
  declarations: [BucketsPage],
})
export class BucketsPageModule {}
