import { APP_INITIALIZER, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { ServerConfigService } from './services/server-config.service';
import { AppConfigService } from './services/app-config.service';

@NgModule({
  declarations: [],
  imports: [HttpClientModule, CommonModule],
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
          await serverConfig.fetchServerSettings();
        },
      deps: [ServerConfigService, AppConfigService],
      multi: true,
    },
  ],
})
export class AppLoadModule {}
