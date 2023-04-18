import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeatureChipComponent } from './feature-chip.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [FeatureChipComponent],
  exports: [FeatureChipComponent],
  imports: [CommonModule, IonicModule],
})
export class FeatureChipModule {}
