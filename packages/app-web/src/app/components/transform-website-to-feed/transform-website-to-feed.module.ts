import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TransformWebsiteToFeedComponent } from './transform-website-to-feed.component';
import { IonicModule } from '@ionic/angular';
import { EmbeddedWebsiteModule } from '../embedded-website/embedded-website.module';
import { NativeFeedModule } from '../native-feed/native-feed.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ResponsiveColumnsModule } from '../responsive-columns/responsive-columns.module';
import { RemoteFeedModalModule } from '../../modals/remote-feed-modal/remote-feed-modal.module';

@NgModule({
  declarations: [TransformWebsiteToFeedComponent],
  exports: [TransformWebsiteToFeedComponent],
  imports: [
    CommonModule,
    IonicModule,
    EmbeddedWebsiteModule,
    NativeFeedModule,
    RemoteFeedModalModule,
    ReactiveFormsModule,
    ResponsiveColumnsModule,
    FormsModule,
  ],
})
export class TransformWebsiteToFeedModule {}
