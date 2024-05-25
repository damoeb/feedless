import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PlayerComponent } from './player.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [PlayerComponent],
  exports: [PlayerComponent],
  imports: [CommonModule, IonicModule],
})
export class PlayerModule {}
