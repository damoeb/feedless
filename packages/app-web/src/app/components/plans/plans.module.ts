import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PlansComponent } from './plans.component';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';

@NgModule({
  declarations: [PlansComponent],
  exports: [PlansComponent],
  imports: [CommonModule, IonicModule, RouterLink]
})
export class PlansModule {
}
