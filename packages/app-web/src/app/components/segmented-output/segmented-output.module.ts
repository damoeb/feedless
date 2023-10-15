import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SegmentedOutputComponent } from './segmented-output.component';
import { IonicModule } from '@ionic/angular';
import { SelectModule } from '../select/select.module';
import { InputTextModule } from 'primeng/inputtext';



@NgModule({
  declarations: [SegmentedOutputComponent],
  exports: [SegmentedOutputComponent],
  imports: [
    CommonModule,
    IonicModule,
    SelectModule,
    InputTextModule
  ]
})
export class SegmentedOutputModule { }
