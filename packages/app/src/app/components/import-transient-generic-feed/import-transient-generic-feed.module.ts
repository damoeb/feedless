import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImportTransientGenericFeedComponent } from './import-transient-generic-feed.component';
import { IonicModule } from '@ionic/angular';
import { ReactiveFormsModule } from '@angular/forms';
import { FeedMetadataFormModule } from '../feed-metadata-form/feed-metadata-form.module';
import { ImporterMetadataFormModule } from '../importer-metadata-form/importer-metadata-form.module';

@NgModule({
  declarations: [ImportTransientGenericFeedComponent],
  exports: [ImportTransientGenericFeedComponent],
  imports: [
    CommonModule,
    IonicModule,
    ReactiveFormsModule,
    FeedMetadataFormModule,
    ImporterMetadataFormModule,
  ],
})
export class ImportTransientGenericFeedModule {}
