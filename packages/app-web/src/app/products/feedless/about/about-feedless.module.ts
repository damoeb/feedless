import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { AboutFeedlessPage } from './about-feedless.page';
import { AboutFeedlessRoutingModule } from './about-feedless-routing.module';
import { ProductHeadlineModule } from '../../../components/product-headline/product-headline.module';
import { ProductHeaderModule } from '../../../components/product-header/product-header.module';
import { SearchbarModule } from '../../../elements/searchbar/searchbar.module';
import { FeedlessHeaderModule } from '../../../components/feedless-header/feedless-header.module';
import { RemoveIfProdModule } from '../../../directives/remove-if-prod/remove-if-prod.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    AboutFeedlessRoutingModule,
    ProductHeadlineModule,
    ProductHeaderModule,
    SearchbarModule,
    FeedlessHeaderModule,
    RemoveIfProdModule,
  ],
  declarations: [AboutFeedlessPage],
})
export class AboutFeedlessModule {}
