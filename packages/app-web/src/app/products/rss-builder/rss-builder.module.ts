import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';

import { RssBuilderPageRoutingModule } from './rss-builder-routing.module';

import { RssBuilderPage } from './rss-builder.page';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AboutRssBuilderPage } from './about/about-rss-builder.page';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { FeedBuilderPageModule } from './feed-builder/feed-builder.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    RssBuilderPageRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    SearchbarModule,
    FeedBuilderPageModule,
  ],
  declarations: [RssBuilderPage, AboutRssBuilderPage],
})
export class RssBuilderPageModule {}
