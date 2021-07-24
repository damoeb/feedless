import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';

import { ReaderComponent } from './reader.component';

@NgModule({
  declarations: [ReaderComponent],
  exports: [ReaderComponent],
  imports: [CommonModule, IonicModule],
})
export class ReaderModule {}
