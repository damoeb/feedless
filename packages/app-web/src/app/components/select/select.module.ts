import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SelectComponent } from './select.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MenuModule } from '../menu/menu.module';

@NgModule({
  declarations: [SelectComponent],
  exports: [SelectComponent],
  imports: [
    CommonModule,
    IonicModule,
    FormsModule,
    MenuModule,
    ReactiveFormsModule,
  ],
})
export class SelectModule {}
