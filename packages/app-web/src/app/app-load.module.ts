import { APP_INITIALIZER, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { ServerSettingsService } from './services/server-settings.service';

@NgModule({
  declarations: [],
  imports: [HttpClientModule, CommonModule],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: (serverSettings: ServerSettingsService) => () =>
        serverSettings.fetchServerSettings(),
      deps: [ServerSettingsService],
      multi: true
    }
  ]
})
export class AppLoadModule {
}
