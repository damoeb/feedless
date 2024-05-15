import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { FeedListRoutingModule } from './feed-list-routing.module';
import { ProductHeadlineModule } from '../../../components/product-headline/product-headline.module';
import { FeedListPage } from './feed-list.page';
import { SearchbarModule } from '../../../elements/searchbar/searchbar.module';
import { ProductHeaderModule } from '../../../components/product-header/product-header.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    FeedListRoutingModule,
    ProductHeadlineModule,
    SearchbarModule,
    ProductHeaderModule,
  ],
  declarations: [FeedListPage],
})
export class FeedListModule {}
