import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedDiscoveryWizardComponent } from './feed-discovery-wizard.component';
import { IonicModule } from '@ionic/angular';
import { ArticleRefModule } from '../article-ref/article-ref.module';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PreviewFeedModalModule } from '../preview-feed-modal/preview-feed-modal.module';
import { BubbleModule } from '../bubble/bubble.module';
import { SearchAddressModalModule } from '../search-address-modal/search-address-modal.module';
import { FeatureToggleModule } from '../../directives/feature-toggle/feature-toggle.module';

@NgModule({
  declarations: [FeedDiscoveryWizardComponent],
  exports: [FeedDiscoveryWizardComponent],
  imports: [
    CommonModule,
    IonicModule,
    ArticleRefModule,
    RouterLink,
    FormsModule,
    PreviewFeedModalModule,
    BubbleModule,
    SearchAddressModalModule,
    FeatureToggleModule,
  ],
})
export class FeedDiscoveryWizardModule {}
