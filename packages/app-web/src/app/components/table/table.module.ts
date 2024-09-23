import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableComponent } from './table.component';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';

@NgModule({
  declarations: [TableComponent],
  exports: [TableComponent],
  imports: [CommonModule, IonicModule, RouterLink],
})
export class TableModule {}
