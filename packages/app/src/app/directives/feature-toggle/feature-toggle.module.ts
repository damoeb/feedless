import { NgModule } from '@angular/core';
import { FeatureToggleDirective } from './feature-toggle.directive';


@NgModule({
  declarations: [FeatureToggleDirective],
  exports: [FeatureToggleDirective],
})
export class FeatureToggleModule {}
