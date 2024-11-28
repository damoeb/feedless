import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedBuilderPageRoutingModule } from './feed-builder-routing.module';
import { FeedBuilderPage } from './feed-builder.page';



import { IonContent } from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FeedBuilderPageRoutingModule,
    IonContent,
    FeedBuilderPage,
],
})
export class FeedBuilderPageModule {}
