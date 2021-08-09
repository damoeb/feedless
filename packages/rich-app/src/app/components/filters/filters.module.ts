import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FiltersComponent } from './filters.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [FiltersComponent],
  exports: [FiltersComponent],
  imports: [CommonModule, IonicModule],
})
export class FiltersModule {}
