import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FilterToolbarComponent } from './filter-toolbar.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [FilterToolbarComponent],
  exports: [FilterToolbarComponent],
  imports: [CommonModule, IonicModule, FormsModule, ReactiveFormsModule],
})
export class FilterToolbarModule {}
