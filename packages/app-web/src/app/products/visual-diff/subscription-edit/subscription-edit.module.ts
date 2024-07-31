import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { SubscriptionEditPage } from './subscription-edit.page';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SubscriptionEditRoutingModule } from './subscription-edit-routing.module';
import { SearchbarModule } from '../../../elements/searchbar/searchbar.module';
import { InteractiveWebsiteModule } from '../../../components/interactive-website/interactive-website.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    FormsModule,
    ReactiveFormsModule,
    SubscriptionEditRoutingModule,
    SearchbarModule,
    InteractiveWebsiteModule,
  ],
  declarations: [SubscriptionEditPage],
})
export class SubscriptionEditPageModule {}
