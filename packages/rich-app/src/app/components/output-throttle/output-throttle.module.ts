import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OutputThrottleComponent } from './output-throttle.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [OutputThrottleComponent],
  exports: [OutputThrottleComponent],
  imports: [CommonModule, IonicModule],
})
export class OutputThrottleModule {}
