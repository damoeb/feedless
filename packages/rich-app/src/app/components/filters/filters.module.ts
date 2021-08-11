import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FiltersComponent } from './filters.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [FiltersComponent],
  exports: [FiltersComponent],
  imports: [CommonModule, IonicModule, FormsModule],
})
export class FiltersModule {}
