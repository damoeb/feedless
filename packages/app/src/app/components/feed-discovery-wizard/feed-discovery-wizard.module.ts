import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedDiscoveryWizardComponent } from './feed-discovery-wizard.component';
import { IonicModule } from '@ionic/angular';
import { ArticleModule } from '../article/article.module';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [FeedDiscoveryWizardComponent],
  exports: [FeedDiscoveryWizardComponent],
  imports: [CommonModule, IonicModule, ArticleModule, RouterLink, FormsModule],
})
export class FeedDiscoveryWizardModule {}
