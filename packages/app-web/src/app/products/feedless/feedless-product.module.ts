import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedlessProductRoutingModule } from './feedless-product-routing.module';

import { FeedlessProductPage } from './feedless-product.page';






import { IonContent, IonRouterOutlet } from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FeedlessProductRoutingModule,
    IonContent,
    IonRouterOutlet,
    FeedlessProductPage,
],
})
export class FeedlessProductModule {}
