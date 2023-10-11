import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedBuilderModalComponent } from './feed-builder-modal.component';
import { IonicModule } from '@ionic/angular';
import { SelectModule } from '../../components/select/select.module';
import { ScrapeSourceModule } from '../../components/scrape-source/scrape-source.module';
import { TransformWebsiteToFeedModule } from '../../components/transform-website-to-feed/transform-website-to-feed.module';
import { FormsModule } from '@angular/forms';
import { MenuModule } from '../../components/menu/menu.module';
import { AgentsModule } from '../../components/agents/agents.module';
import { SplitterModule } from 'primeng/splitter';
import { InputTextModule } from 'primeng/inputtext';
import { FeedBuilderCardModule } from '../../components/feed-builder-card/feed-builder-card.module';

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
    SplitterModule,
    InputTextModule,
    FeedBuilderCardModule
  ]
})
export class FeedBuilderModalModule {}
