import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AboutRssBuilderRoutingModule } from './about-rss-builder-routing.module';
import { ProductHeadlineModule } from '../../../components/product-headline/product-headline.module';
import { AboutRssBuilderPage } from './about-rss-builder.page';
import { SearchbarModule } from '../../../elements/searchbar/searchbar.module';
import { ImportOpmlModalModule } from '../../../modals/import-opml-modal/import-opml-modal.module';
import { ProductHeaderModule } from '../../../components/product-header/product-header.module';
import { ImportButtonModule } from '../../../components/import-button/import-button.module';
import {
  IonContent,
  IonButton,
  IonIcon,
  IonItem,
} from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    AboutRssBuilderRoutingModule,
    ProductHeadlineModule,
    SearchbarModule,
    ImportOpmlModalModule,
    ProductHeaderModule,
    ImportButtonModule,
    IonContent,
    IonButton,
    IonIcon,
    IonItem,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [AboutRssBuilderPage],
})
export class AboutRssBuilderModule {}
