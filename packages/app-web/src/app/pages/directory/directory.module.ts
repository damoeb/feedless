import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { DirectoryPage } from './directory.page';
import 'img-comparison-slider';
import { DirectoryRoutingModule } from './directory-routing.module';
import { BubbleModule } from '../../components/bubble/bubble.module';
import { HistogramModule } from '../../components/histogram/histogram.module';
import { ImportButtonModule } from '../../components/import-button/import-button.module';
import { PaginationModule } from '../../components/pagination/pagination.module';
import { ProductHeaderModule } from '../../components/product-header/product-header.module';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { ReactiveFormsModule } from '@angular/forms';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    DirectoryRoutingModule,
    BubbleModule,
    HistogramModule,
    ImportButtonModule,
    PaginationModule,
    ProductHeaderModule,
    SearchbarModule,
    ReactiveFormsModule,
    FeedlessHeaderModule,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [DirectoryPage],
})
export class DirectoryPageModule {}
