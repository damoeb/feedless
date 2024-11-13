import { APP_INITIALIZER, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  provideHttpClient,
  withInterceptorsFromDi,
} from '@angular/common/http';
import { ServerConfigService } from './services/server-config.service';
import { AppConfigService } from './services/app-config.service';

@NgModule({
  declarations: [],
  imports: [CommonModule],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory:
        (
          serverConfig: ServerConfigService,
          appConfigService: AppConfigService,
        ) =>
        async () => {
          await appConfigService.activateUserInterface(
            await serverConfig.fetchConfig(),
          );
          // todo remove
          await serverConfig.fetchServerSettings();
        },
      deps: [ServerConfigService, AppConfigService],
      multi: true,
    },
    provideHttpClient(withInterceptorsFromDi()),
  ],
})
export class AppLoadModule {}
