import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { UpcomingProductRoutingModule } from './upcoming-product-routing.module';






// import { HttpClient } from '@angular/common/http';
// import dayjs from 'dayjs';
import { EventsPageModule } from './events/events-page.module';
import { EventPageModule } from './event/event-page.module';
import { AboutUsPage } from './about-us/about-us.page';
import { TermsPage } from './terms/terms.page';
import { IonContent, IonHeader } from '@ionic/angular/standalone';

// export function createTranslateLoader(http: HttpClient) {
//   return new TranslateHttpLoader(http, './assets/i18n/upcoming/', '.json');
// }

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    UpcomingProductRoutingModule,
    IonContent,
    // TranslateModule.forRoot({
    //   isolate: true,
    //   extend: false,
    //   loader: {
    //     provide: TranslateLoader,
    //     useFactory: createTranslateLoader,
    //     deps: [HttpClient],
    //   },
    // }),
    ReactiveFormsModule,
    EventsPageModule,
    EventPageModule,
    IonHeader,
    AboutUsPage,
    TermsPage,
],
  exports: [AboutUsPage, TermsPage],
})
export class UpcomingProductModule {
  // constructor(translateService: TranslateService) {
  //   const defaultLanguage = 'de';
  //   translateService.setDefaultLang(defaultLanguage);
  //   translateService.langs = ['en', 'de'];
  //   const browserLang = translateService.getBrowserLang();
  //   if (translateService.langs.includes(browserLang)) {
  //     translateService.use(browserLang);
  //   } else {
  //     translateService.use(defaultLanguage);
  //   }
  //   translateService.onLangChange.subscribe((lang) => {
  //     dayjs.locale(lang.lang);
  //   });
  // }
}
