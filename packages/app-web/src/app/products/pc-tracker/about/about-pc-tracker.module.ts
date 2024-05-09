import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { AboutPcTrackerRoutingModule } from './about-pc-tracker-routing.module';
import { ProductHeadlineModule } from '../../../components/product-headline/product-headline.module';
import { AboutPcTrackerPage } from './about-pc-tracker.page';
import { SearchbarModule } from '../../../elements/searchbar/searchbar.module';
import { ProductHeaderModule } from '../../../components/product-header/product-header.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    AboutPcTrackerRoutingModule,
    ProductHeadlineModule,
    SearchbarModule,
    ProductHeaderModule,
  ],
  declarations: [AboutPcTrackerPage],
})
export class AboutPcTrackerModule {}
