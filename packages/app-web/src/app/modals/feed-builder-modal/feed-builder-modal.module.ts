import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedBuilderModalComponent } from './feed-builder-modal.component';
import { IonicModule } from '@ionic/angular';
import { ScrapeSourceModule } from '../../components/scrape-source/scrape-source.module';
import { TransformWebsiteToFeedModule } from '../../components/transform-website-to-feed/transform-website-to-feed.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MenuModule } from '../../elements/menu/menu.module';
import { AgentsModule } from '../../components/agents/agents.module';
import { SegmentedOutputModule } from '../../components/segmented-output/segmented-output.module';
import { SelectModule } from '../../elements/select/select.module';
import { InputModule } from '../../elements/input/input.module';
import { AgentsModalModule } from '../agents-modal/agents-modal.module';
import { CodeEditorModalModule } from '../code-editor-modal/code-editor-modal.module';

@NgModule({
  declarations: [FeedBuilderModalComponent],
  exports: [FeedBuilderModalComponent],
  imports: [
    CommonModule,
    IonicModule,
    ScrapeSourceModule,
    TransformWebsiteToFeedModule,
    FormsModule,
    MenuModule,
    AgentsModule,
    SegmentedOutputModule,
    ReactiveFormsModule,
    SelectModule,
    InputModule,
    AgentsModalModule,
    CodeEditorModalModule
  ]
})
export class FeedBuilderModalModule {
}
