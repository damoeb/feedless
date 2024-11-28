import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AboutRssBuilderRoutingModule } from './about-rss-builder-routing.module';

import { AboutRssBuilderPage } from './about-rss-builder.page';

import { ImportOpmlModalModule } from '../../../modals/import-opml-modal/import-opml-modal.module';

import { ImportButtonModule } from '../../../components/import-button/import-button.module';
import {
  IonButton,
  IonContent,
  IonIcon,
  IonItem,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    AboutRssBuilderRoutingModule,
    ImportOpmlModalModule,
    ImportButtonModule,
    IonContent,
    IonButton,
    IonIcon,
    IonItem,
    AboutRssBuilderPage,
],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class AboutRssBuilderModule {}
