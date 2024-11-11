import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PlayerComponent } from './player.component';
import { IonButton, IonIcon, IonNote } from '@ionic/angular/standalone';

@NgModule({
  declarations: [PlayerComponent],
  exports: [PlayerComponent],
  imports: [CommonModule, IonButton, IonIcon, IonNote],
})
export class PlayerModule {}
