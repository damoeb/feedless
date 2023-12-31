import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SegmentedOutputComponent } from './segmented-output.component';
import { IonicModule } from '@ionic/angular';
import { SelectModule } from '../select/select.module';
import { InputModule } from '../../elements/input/input.module';

@NgModule({
  declarations: [SegmentedOutputComponent],
  exports: [SegmentedOutputComponent],
  imports: [CommonModule, IonicModule, SelectModule, InputModule],
})
export class SegmentedOutputModule {}
