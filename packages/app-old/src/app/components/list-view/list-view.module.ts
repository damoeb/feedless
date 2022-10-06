import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ListViewComponent } from './list-view.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [ListViewComponent],
  exports: [ListViewComponent],
  imports: [CommonModule, IonicModule],
})
export class ListViewModule {}
