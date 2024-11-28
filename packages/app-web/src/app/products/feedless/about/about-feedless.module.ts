import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AboutFeedlessPage } from './about-feedless.page';
import { AboutFeedlessRoutingModule } from './about-feedless-routing.module';





import {
  IonBadge,
  IonContent,
  IonIcon,
  IonItem,
  IonList,
  IonListHeader,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    AboutFeedlessRoutingModule,
    IonContent,
    IonList,
    IonListHeader,
    IonItem,
    IonIcon,
    IonBadge,
    AboutFeedlessPage,
],
})
export class AboutFeedlessModule {}
