import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScrapeSourceComponent } from './scrape-source.component';
import { EmbeddedWebsiteModule } from '../embedded-website/embedded-website.module';
import { FormsModule } from '@angular/forms';
import { IonicModule } from '@ionic/angular';
import { ScrapeActionsModule } from '../scrape-actions/scrape-actions.module';
import { SelectModule } from '../select/select.module';



@NgModule({
  declarations: [ScrapeSourceComponent],
  exports: [ScrapeSourceComponent],
  imports: [
    CommonModule,
    EmbeddedWebsiteModule,
    FormsModule,
    IonicModule,
    ScrapeActionsModule,
    SelectModule
  ]
})
export class ScrapeSourceModule { }
