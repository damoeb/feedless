import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';

import { ReaderComponent } from './reader.component';
import { RouterModule } from '@angular/router';

@NgModule({
  declarations: [ReaderComponent],
  exports: [ReaderComponent],
  imports: [CommonModule, IonicModule, RouterModule],
})
export class ReaderModule {}
