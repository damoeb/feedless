import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DiscoveryModalComponent } from './discovery-modal.component';
import { IonicModule } from '@ionic/angular';
import { ArticleModule } from '../article/article.module';
import { RouterLink } from '@angular/router';
import { FeedDiscoveryWizardModule } from '../feed-discovery-wizard/feed-discovery-wizard.module';

@NgModule({
  declarations: [DiscoveryModalComponent],
  exports: [DiscoveryModalComponent],
  imports: [
    CommonModule,
    IonicModule,
    ArticleModule,
    RouterLink,
    FeedDiscoveryWizardModule,
  ],
})
export class DiscoveryModalModule {}
