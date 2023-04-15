import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EnclosureComponent } from './enclosure.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [EnclosureComponent],
  exports: [EnclosureComponent],
  imports: [CommonModule, IonicModule],
})
export class EnclosureModule {}
