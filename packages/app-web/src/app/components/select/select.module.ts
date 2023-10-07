import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SelectComponent } from './select.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule } from '@angular/forms';



@NgModule({
  declarations: [SelectComponent],
  exports: [SelectComponent],
  imports: [
    CommonModule,
    IonicModule,
    FormsModule
  ]
})
export class SelectModule { }
