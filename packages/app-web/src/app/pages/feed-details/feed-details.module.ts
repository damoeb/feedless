import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedDetailsPage } from './feed-details.page';
import 'img-comparison-slider';
import { FeedDetailsRoutingModule } from './feed-details-routing.module';




import { FormsModule } from '@angular/forms';

import { FeedDetailsModule } from '../../components/feed-details/feed-details.module';

import {
  IonBreadcrumb,
  IonBreadcrumbs,
  IonButton,
  IonContent,
  IonHeader,
  IonItem,
  IonSpinner,
  IonText,
  IonToolbar,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FeedDetailsRoutingModule,
    FormsModule,
    FeedDetailsModule,
    IonHeader,
    IonToolbar,
    IonText,
    IonButton,
    IonContent,
    IonBreadcrumbs,
    IonBreadcrumb,
    IonSpinner,
    IonItem,
    FeedDetailsPage,
],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class FeedDetailsPageModule {}
