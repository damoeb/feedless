import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AboutPcTrackerRoutingModule } from './about-pc-tracker-routing.module';
import { ProductHeadlineModule } from '../../../components/product-headline/product-headline.module';
import { AboutPcTrackerPage } from './about-pc-tracker.page';
import { SearchbarModule } from '../../../elements/searchbar/searchbar.module';
import { ProductHeaderModule } from '../../../components/product-header/product-header.module';
import { IonContent } from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    AboutPcTrackerRoutingModule,
    ProductHeadlineModule,
    SearchbarModule,
    ProductHeaderModule,
    IonContent,
  ],
  declarations: [AboutPcTrackerPage],
})
export class AboutPcTrackerModule {}
