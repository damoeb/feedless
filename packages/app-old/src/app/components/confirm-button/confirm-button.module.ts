import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfirmButtonComponent } from './confirm-button.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [ConfirmButtonComponent],
  exports: [ConfirmButtonComponent],
  imports: [CommonModule, IonicModule],
})
export class ConfirmButtonModule {}
