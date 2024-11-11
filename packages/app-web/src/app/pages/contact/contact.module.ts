import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ContactPageRoutingModule } from './contact-routing.module';

import { ContactPage } from './contact.page';
import { FeedlessHeaderModule } from '../../components/feedless-header/feedless-header.module';
import { IonContent } from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ContactPageRoutingModule,
    FeedlessHeaderModule,
    IonContent,
  ],
  declarations: [ContactPage],
})
export class ContactPageModule {}
