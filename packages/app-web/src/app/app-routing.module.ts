import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule } from '@angular/router';

@NgModule({
  declarations: [],
  imports: [
    RouterModule.forRoot([], {
      preloadingStrategy: PreloadAllModules,
      paramsInheritanceStrategy: 'always',
    }),
  ],
})
export class AppRoutingModule {}
