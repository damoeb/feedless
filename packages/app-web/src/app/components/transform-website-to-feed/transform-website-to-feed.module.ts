import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TransformWebsiteToFeedComponent } from './transform-website-to-feed.component';
import { IonicModule } from '@ionic/angular';
import { EmbeddedWebsiteModule } from '../embedded-website/embedded-website.module';
import { RemoteFeedModule } from '../remote-feed/remote-feed.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ResponsiveColumnsModule } from '../responsive-columns/responsive-columns.module';

@NgModule({
  declarations: [TransformWebsiteToFeedComponent],
  exports: [TransformWebsiteToFeedComponent],
  imports: [
    CommonModule,
    IonicModule,
    EmbeddedWebsiteModule,
    RemoteFeedModule,
    ReactiveFormsModule,
    ResponsiveColumnsModule,
    FormsModule,
  ],
})
export class TransformWebsiteToFeedModule {}
