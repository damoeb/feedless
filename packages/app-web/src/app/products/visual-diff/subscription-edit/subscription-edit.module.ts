import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { SubscriptionEditPage } from './subscription-edit.page';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { EmbeddedImageModule } from '../../../components/embedded-image/embedded-image.module';
import { SubscriptionEditRoutingModule } from './subscription-edit-routing.module';
import { SearchbarModule } from '../../../elements/searchbar/searchbar.module';
import { EmbeddedWebsiteModule } from '../../../components/embedded-website/embedded-website.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    FormsModule,
    ReactiveFormsModule,
    EmbeddedImageModule,
    SubscriptionEditRoutingModule,
    SearchbarModule,
    EmbeddedWebsiteModule,
  ],
  declarations: [SubscriptionEditPage],
})
export class SubscriptionEditPageModule {}
