import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedDiscoveryWizardComponent } from './feed-discovery-wizard.component';
import { IonicModule } from '@ionic/angular';
import { ArticleModule } from '../article/article.module';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PreviewFeedModalModule } from '../preview-feed-modal/preview-feed-modal.module';
import { BubbleModule } from '../bubble/bubble.module';

@NgModule({
  declarations: [FeedDiscoveryWizardComponent],
  exports: [FeedDiscoveryWizardComponent],
  imports: [
    CommonModule,
    IonicModule,
    ArticleModule,
    RouterLink,
    FormsModule,
    PreviewFeedModalModule,
    BubbleModule
  ]
})
export class FeedDiscoveryWizardModule {}
