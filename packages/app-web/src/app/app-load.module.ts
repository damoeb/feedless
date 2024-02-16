import { APP_INITIALIZER, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { ServerSettingsService } from './services/server-settings.service';
import { ProductService } from './services/product.service';

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
          const product = await serverSettings.fetchConfig();
          productService.activateProduct(product);
          await serverSettings.fetchServerSettings();
        },
      deps: [ServerSettingsService, ProductService],
      multi: true,
    },
  ],
})
export class AppLoadModule {}
