import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ItemsFilterModalComponent } from './items-filter-modal.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule } from '@angular/forms';



@NgModule({
  declarations: [ItemsFilterModalComponent],
  exports: [ItemsFilterModalComponent],
  imports: [
    CommonModule,
    IonicModule,
    FormsModule
  ]
})
export class ItemsFilterModalModule { }
