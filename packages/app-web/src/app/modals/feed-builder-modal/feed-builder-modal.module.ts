import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedBuilderModalComponent } from './feed-builder-modal.component';
import { IonicModule } from '@ionic/angular';
import { SelectModule } from '../../components/select/select.module';
import { ScrapeSourceModule } from '../../components/scrape-source/scrape-source.module';
import { TransformWebsiteToFeedModule } from '../../components/transform-website-to-feed/transform-website-to-feed.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MenuModule } from '../../components/menu/menu.module';
import { AgentsModule } from '../../components/agents/agents.module';
import { FeedBuilderCardModule } from '../../components/feed-builder-card/feed-builder-card.module';
import { SegmentedOutputModule } from '../../components/segmented-output/segmented-output.module';
import { Select2Module } from '../../components/select2/select2.module';
import { BucketsModalModule } from '../buckets-modal/buckets-modal.module';
import { InputModule } from '../../elements/input/input.module';
import { AgentsModalModule } from '../agents-modal/agents-modal.module';
import { CodeEditorModalModule } from '../code-editor-modal/code-editor-modal.module';

@NgModule({
  declarations: [FeedBuilderModalComponent],
  exports: [FeedBuilderModalComponent],
  imports: [
    CommonModule,
    IonicModule,
    SelectModule,
    ScrapeSourceModule,
    TransformWebsiteToFeedModule,
    FormsModule,
    MenuModule,
    AgentsModule,
    FeedBuilderCardModule,
    SegmentedOutputModule,
    ReactiveFormsModule,
    Select2Module,
    BucketsModalModule,
    InputModule,
    AgentsModalModule,
    CodeEditorModalModule,
  ]
})
export class FeedBuilderModalModule {}
