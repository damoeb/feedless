import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { FeedTilesRoutingModule } from './feed-tiles-routing.module';
import { ProductHeadlineModule } from '../../../components/product-headline/product-headline.module';
import { FeedTilesPage } from './feed-tiles.page';
import { SearchbarModule } from '../../../elements/searchbar/searchbar.module';
import { ProductHeaderModule } from '../../../components/product-header/product-header.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    FeedTilesRoutingModule,
    ProductHeadlineModule,
    SearchbarModule,
    ProductHeaderModule,
  ],
  declarations: [FeedTilesPage],
})
export class FeedTilesModule {}
