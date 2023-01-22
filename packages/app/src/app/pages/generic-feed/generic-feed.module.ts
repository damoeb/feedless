import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { DiscoveryWizardPageRoutingModule } from './generic-feed-routing.module';

import { GenericFeedPage } from './generic-feed.page';
import { FeedDiscoveryWizardModule } from '../../components/feed-discovery-wizard/feed-discovery-wizard.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    DiscoveryWizardPageRoutingModule,
    FeedDiscoveryWizardModule,
  ],
  declarations: [GenericFeedPage],
})
export class GenericFeedPageModule {}
