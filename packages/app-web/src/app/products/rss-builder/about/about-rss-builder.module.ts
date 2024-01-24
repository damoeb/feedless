import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { AboutRssBuilderRoutingModule } from './about-rss-builder-routing.module';
import { ProductHeadlineModule } from '../../../components/product-headline/product-headline.module';
import { AboutRssBuilderPage } from './about-rss-builder.page';
import { SearchbarModule } from '../../../elements/searchbar/searchbar.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    AboutRssBuilderRoutingModule,
    ProductHeadlineModule,
    SearchbarModule
  ],
  declarations: [AboutRssBuilderPage]
})
export class AboutRssBuilderModule {
}
