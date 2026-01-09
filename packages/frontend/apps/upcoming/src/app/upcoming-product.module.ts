import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

// import { HttpClient } from '@angular/common/http';
// import dayjs from 'dayjs';
import { IonContent } from '@ionic/angular/standalone';

// export function createTranslateLoader(http: HttpClient) {
//   return new TranslateHttpLoader(http, './assets/i18n/upcoming/', '.json');
// }

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
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
  ],
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
