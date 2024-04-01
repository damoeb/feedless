import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OpmlPickerComponent } from './opml-picker.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [OpmlPickerComponent],
  exports: [OpmlPickerComponent],
  imports: [CommonModule, IonicModule]
})
export class OpmlPickerModule {}
