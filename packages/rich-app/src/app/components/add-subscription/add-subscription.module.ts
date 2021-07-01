import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AddSubscriptionComponent } from './add-subscription.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [AddSubscriptionComponent],
  exports: [AddSubscriptionComponent],
  imports: [CommonModule, IonicModule, FormsModule],
})
export class AddSubscriptionModule {}
