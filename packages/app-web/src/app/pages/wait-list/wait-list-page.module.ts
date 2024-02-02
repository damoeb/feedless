import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WaitListPage } from './wait-list.page';
import { IonicModule } from '@ionic/angular';
import { NewsletterModule } from '../../components/newsletter/newsletter.module';
import { WaitListRoutingModule } from './wait-list-routing.module';

@NgModule({
  declarations: [WaitListPage],
  exports: [WaitListPage],
  imports: [CommonModule, IonicModule, NewsletterModule, WaitListRoutingModule]
})
export class WaitListPageModule {
}
