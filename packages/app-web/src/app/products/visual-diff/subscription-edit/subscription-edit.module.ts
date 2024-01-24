import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { SubscriptionEditPage } from './subscription-edit.page';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { EmbeddedImageModule } from '../../../components/embedded-image/embedded-image.module';
import { SubscriptionEditRoutingModule } from './subscription-edit-routing.module';
import { SearchbarModule } from '../../../elements/searchbar/searchbar.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    FormsModule,
    ReactiveFormsModule,
    EmbeddedImageModule,
    SubscriptionEditRoutingModule,
    SearchbarModule
  ],
  declarations: [SubscriptionEditPage]
})
export class SubscriptionEditPageModule {
}
