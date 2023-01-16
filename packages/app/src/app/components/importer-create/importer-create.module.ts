import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { ImporterCreatePage } from './importer-create.page';
import { ImportTransientNativeFeedModule } from '../import-transient-native-feed/import-transient-native-feed.module';
import { DiscoveryModalModule } from '../discovery-modal/discovery-modal.module';
import { ImportTransientGenericFeedModule } from '../import-transient-generic-feed/import-transient-generic-feed.module';
import { ImportExistingNativeFeedModule } from '../import-existing-native-feed/import-existing-native-feed.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    ReactiveFormsModule,
    DiscoveryModalModule,
    ImportTransientNativeFeedModule,
    ImportTransientGenericFeedModule,
    ImportExistingNativeFeedModule,
  ],
  declarations: [ImporterCreatePage],
})
export class ImporterEditPageModule {}
