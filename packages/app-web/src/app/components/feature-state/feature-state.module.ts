import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeatureStateComponent } from './feature-state.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [FeatureStateComponent],
  exports: [FeatureStateComponent],
  imports: [CommonModule, IonicModule],
})
export class FeatureStateModule {}
