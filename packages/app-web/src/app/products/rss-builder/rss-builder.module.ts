import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';

import { RssBuilderPageRoutingModule } from './rss-builder-routing.module';

import { RssBuilderPage } from './rss-builder.page';
import { PageHeaderModule } from '../../components/page-header/page-header.module';
import { EmbeddedWebsiteModule } from '../../components/embedded-website/embedded-website.module';
import { TransformWebsiteToFeedModule } from '../../components/transform-website-to-feed/transform-website-to-feed.module';
import { FormsModule } from '@angular/forms';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    RssBuilderPageRoutingModule,
    PageHeaderModule,
    EmbeddedWebsiteModule,
    TransformWebsiteToFeedModule,
    FormsModule
  ],
  declarations: [RssBuilderPage],
})
export class RssBuilderPageModule {}
