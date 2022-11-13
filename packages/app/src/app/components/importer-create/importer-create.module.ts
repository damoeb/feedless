import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { ImporterCreatePage } from './importer-create.page';
import { PreviewTransientNativeFeedModule } from '../preview-transient-native-feed/preview-transient-native-feed.module';
import { PreviewTransientGenericFeedModule } from '../preview-transient-generic-feed/preview-transient-generic-feed.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    ReactiveFormsModule,
    PreviewTransientNativeFeedModule,
    PreviewTransientGenericFeedModule,
  ],
  declarations: [ImporterCreatePage],
})
export class ImporterEditPageModule {}
