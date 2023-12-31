import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Select2Component } from './select2.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MenuModule } from '../menu/menu.module';

@NgModule({
  declarations: [Select2Component],
  exports: [Select2Component],
  imports: [
    CommonModule,
    IonicModule,
    FormsModule,
    MenuModule,
    ReactiveFormsModule,
  ],
})
export class Select2Module {}
