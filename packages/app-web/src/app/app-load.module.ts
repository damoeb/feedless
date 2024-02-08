import { APP_INITIALIZER, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { ServerSettingsService } from './services/server-settings.service';
import { ProductService } from './services/product.service';
import { environment } from '../environments/environment';

@NgModule({
  declarations: [],
  imports: [HttpClientModule, CommonModule],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory:
        (
          serverSettings: ServerSettingsService,
          productService: ProductService,
        ) =>
        async () => {
          await serverSettings.fetchServerSettings();
          productService.activateProduct(environment.product());
        },
      deps: [ServerSettingsService, ProductService],
      multi: true,
    },
  ],
})
export class AppLoadModule {}
