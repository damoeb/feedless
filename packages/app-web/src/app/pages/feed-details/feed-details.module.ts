import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedDetailsPage } from './feed-details.page';
import 'img-comparison-slider';
import { FeedDetailsRoutingModule } from './feed-details-routing.module';
import { BubbleModule } from '../../components/bubble/bubble.module';
import { LoginButtonModule } from '../../components/login-button/login-button.module';
import { HistogramModule } from '../../components/histogram/histogram.module';
import { ReaderModule } from '../../components/reader/reader.module';
import { FormsModule } from '@angular/forms';
import { TagsModalModule } from '../../modals/tags-modal/tags-modal.module';
import { FeedDetailsModule } from '../../components/feed-details/feed-details.module';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';
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
    BubbleModule,
    LoginButtonModule,
    HistogramModule,
    TagsModalModule,
    ReaderModule,
    FormsModule,
    FeedDetailsModule,
    FeedlessHeaderModule,
    IonHeader,
    IonToolbar,
    IonText,
    IonButton,
    IonContent,
    IonBreadcrumbs,
    IonBreadcrumb,
    IonSpinner,
    IonItem,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [FeedDetailsPage],
})
export class FeedDetailsPageModule {}
