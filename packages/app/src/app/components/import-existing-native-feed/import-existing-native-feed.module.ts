import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImportExistingNativeFeedComponent } from './import-existing-native-feed.component';
import { IonicModule } from '@ionic/angular';
import { ReactiveFormsModule } from '@angular/forms';
import { FeedMetadataFormModule } from '../feed-metadata-form/feed-metadata-form.module';
import { ImporterMetadataFormModule } from '../importer-metadata-form/importer-metadata-form.module';

@NgModule({
  declarations: [ImportExistingNativeFeedComponent],
  exports: [ImportExistingNativeFeedComponent],
  imports: [CommonModule, IonicModule, ReactiveFormsModule, FeedMetadataFormModule, ImporterMetadataFormModule]
})
export class ImportExistingNativeFeedModule {}
