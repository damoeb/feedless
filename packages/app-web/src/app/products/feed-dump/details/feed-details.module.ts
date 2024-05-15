import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { FeedDetailsRoutingModule } from './feed-details-routing.module';
import { ProductHeadlineModule } from '../../../components/product-headline/product-headline.module';
import { FeedDetailsPage } from './feed-details.page';
import { SearchbarModule } from '../../../elements/searchbar/searchbar.module';
import { ProductHeaderModule } from '../../../components/product-header/product-header.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    FeedDetailsRoutingModule,
    ProductHeadlineModule,
    SearchbarModule,
    ProductHeaderModule,
  ],
  declarations: [FeedDetailsPage],
})
export class FeedDetailsModule {}
