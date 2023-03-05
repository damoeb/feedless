import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SubscribeModalComponent } from './subscribe-modal.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [SubscribeModalComponent],
  exports: [SubscribeModalComponent],
  imports: [CommonModule, IonicModule],
})
export class SubscribeModalModule {}
