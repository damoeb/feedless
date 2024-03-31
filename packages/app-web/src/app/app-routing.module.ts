import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule } from '@angular/router';
import { environment } from '../environments/environment';

@NgModule({
  declarations: [],
  imports: [
    RouterModule.forRoot([], {
      preloadingStrategy: PreloadAllModules,
      paramsInheritanceStrategy: 'always',
      enableTracing: !environment.production
    }),
  ],
})
export class AppRoutingModule {}
