import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { SubscriptionsPage } from './subscriptions.page';
import { SubscriptionsRoutingModule } from './subscriptions-routing.module';

@NgModule({
  imports: [CommonModule, IonicModule, SubscriptionsRoutingModule],
  declarations: [SubscriptionsPage],
})
export class SubscriptionsPageModule {}
