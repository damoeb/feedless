import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InputComponent } from './input.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [InputComponent],
  exports: [InputComponent],
  imports: [CommonModule, IonicModule, FormsModule, ReactiveFormsModule],
})
export class InputModule {}
