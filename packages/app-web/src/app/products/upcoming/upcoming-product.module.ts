import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';
import { TranslateLoader, TranslateModule, TranslateService } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { UpcomingProductRoutingModule } from './upcoming-product-routing.module';

import { DarkModeButtonModule } from '../../components/dark-mode-button/dark-mode-button.module';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { BubbleModule } from '../../components/bubble/bubble.module';
import { MapModule } from '../../components/map/map.module';
import { MapModalModule } from '../../modals/map-modal/map-modal.module';
import { HttpClient } from '@angular/common/http';
import dayjs from 'dayjs';
import { EventsPageModule } from './events/events-page.module';
import { EventPageModule } from './event/event-page.module';

export function createTranslateLoader(http: HttpClient) {
  return new TranslateHttpLoader(http, './assets/i18n/upcoming/', '.json');
}

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    UpcomingProductRoutingModule,
    DarkModeButtonModule,
    SearchbarModule,
    BubbleModule,
    MapModule,
    MapModalModule,
    TranslateModule.forRoot({
      isolate: true,
      extend: false,
      loader: {
        provide: TranslateLoader,
        useFactory: createTranslateLoader,
        deps: [HttpClient],
      },
    }),
    ReactiveFormsModule,
    EventsPageModule,
    EventPageModule,
  ],
})
export class UpcomingProductModule {
  constructor(translateService: TranslateService) {
    const defaultLanguage = 'de';
    translateService.setDefaultLang(defaultLanguage);
    translateService.langs = ['en', 'de'];
    const browserLang = translateService.getBrowserLang();
    if (translateService.langs.includes(browserLang)) {
      translateService.use(browserLang);
    } else {
      translateService.use(defaultLanguage);
    }
    translateService.onLangChange.subscribe((lang) => {
      dayjs.locale(lang.lang);
    });
  }
}
