import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EnclosuresComponent } from './enclosures.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [EnclosuresComponent],
  exports: [EnclosuresComponent],
  imports: [CommonModule, IonicModule],
})
export class EnclosuresModule {}
