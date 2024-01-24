import { APP_INITIALIZER, NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule } from '@angular/router';
import { environment } from '../environments/environment';
import { ProductService } from './services/product.service';

@NgModule({
  declarations: [],
  imports: [
    RouterModule.forRoot([], {
      preloadingStrategy: PreloadAllModules,
      paramsInheritanceStrategy: 'always'
    })
  ],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: (productService: ProductService) => async () => {
        productService.activateProduct(environment.product());
      },
      deps: [ProductService],
      multi: true
    }
  ]
})
export class AppRoutingModule {
}
