import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImportersComponent } from './importers.component';
import { IonicModule } from '@ionic/angular';
import { FilterToolbarModule } from '../filter-toolbar/filter-toolbar.module';
import { RouterLink } from '@angular/router';
import { HistogramModule } from '../histogram/histogram.module';

@NgModule({
  declarations: [ImportersComponent],
  exports: [ImportersComponent],
  imports: [
    CommonModule,
    IonicModule,
    FilterToolbarModule,
    RouterLink,
    HistogramModule,
  ],
})
export class ImportersModule {}
