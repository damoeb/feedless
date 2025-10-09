import { inject, NgModule, provideAppInitializer } from '@angular/core';
import { CommonModule } from '@angular/common';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { ServerConfigService } from './services/server-config.service';
import { AppConfigService } from './services/app-config.service';

@NgModule({
  declarations: [],
  imports: [CommonModule],
  providers: [
    provideAppInitializer(() => {
      const initializerFn = (
        (serverConfig: ServerConfigService, appConfigService: AppConfigService) => async () => {
          await appConfigService.activateUserInterface(await serverConfig.fetchConfig());
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
