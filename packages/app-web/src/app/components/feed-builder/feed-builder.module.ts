import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedBuilderComponent } from './feed-builder.component';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { TransformWebsiteToFeedModule } from '../transform-website-to-feed/transform-website-to-feed.module';
import { InteractiveWebsiteModalModule } from '../../modals/interactive-website-modal/interactive-website-modal.module';
import { TagsModalModule } from '../../modals/tags-modal/tags-modal.module';
import { SearchAddressModalModule } from '../../modals/search-address-modal/search-address-modal.module';
import { FilterItemsAccordionModule } from '../filter-items-accordion/filter-items-accordion.module';
import { FetchRateAccordionModule } from '../fetch-rate-accordion/fetch-rate-accordion.module';
import {
  IonToolbar,
  IonProgressBar,
  IonList,
  IonItem,
  IonLabel,
  IonIcon,
  IonAccordion,
  IonNote,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    TransformWebsiteToFeedModule,
    TagsModalModule,
    InteractiveWebsiteModalModule,
    SearchbarModule,
    SearchAddressModalModule,
    FilterItemsAccordionModule,
    FetchRateAccordionModule,
    IonToolbar,
    IonProgressBar,
    IonList,
    IonItem,
    IonLabel,
    IonIcon,
    IonAccordion,
    IonNote,
  ],
  declarations: [FeedBuilderComponent],
  exports: [FeedBuilderComponent],
})
export class FeedBuilderModule {}
