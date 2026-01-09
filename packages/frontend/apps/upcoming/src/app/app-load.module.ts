import { inject, NgModule, PLATFORM_ID, provideAppInitializer } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import {
  provideHttpClient,
  withInterceptorsFromDi,
} from '@angular/common/http';
import { AppConfigService, ServerConfigService } from '@feedless/services';

@NgModule({
  declarations: [],
  imports: [CommonModule],
  providers: [
    provideAppInitializer(() => {
      const platformId = inject(PLATFORM_ID);

      if (!isPlatformBrowser(platformId)) {
        return Promise.resolve();
      }

      const initializerFn = (
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
        }
      )(inject(ServerConfigService), inject(AppConfigService));
      return initializerFn();
    }),
    provideHttpClient(withInterceptorsFromDi()),
  ],
})
export class AppLoadModule {}
