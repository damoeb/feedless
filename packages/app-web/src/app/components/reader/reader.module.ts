import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReaderComponent } from './reader.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [ReaderComponent],
  exports: [ReaderComponent],
  imports: [CommonModule, IonicModule],
})
export class ReaderModule {}
