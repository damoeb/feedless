import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImportersComponent } from './importers.component';
import { IonicModule } from '@ionic/angular';
import { FilterToolbarModule } from '../filter-toolbar/filter-toolbar.module';

@NgModule({
  declarations: [ImportersComponent],
  exports: [ImportersComponent],
  imports: [CommonModule, IonicModule, FilterToolbarModule],
})
export class ImportersModule {}
