import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { DiscoveryWizardPageRoutingModule } from './discovery-wizard-routing.module';

import { DiscoveryWizardPage } from './discovery-wizard.page';
import { FeedDiscoveryWizardModule } from '../../components/feed-discovery-wizard/feed-discovery-wizard.module';
import { FeedMetadataFormModule } from '../../components/feed-metadata-form/feed-metadata-form.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    DiscoveryWizardPageRoutingModule,
    FeedDiscoveryWizardModule,
    FeedMetadataFormModule
  ],
  declarations: [DiscoveryWizardPage],
})
export class DiscoveryWizardPageModule {}
