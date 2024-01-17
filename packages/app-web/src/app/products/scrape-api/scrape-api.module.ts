import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { ScrapeApiPageRoutingModule } from './scrape-api-routing.module';

import { ScrapeApiPage } from './scrape-api.page';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    ScrapeApiPageRoutingModule,
  ],
  declarations: [ScrapeApiPage],
})
export class ScrapeApiPageModule {}
