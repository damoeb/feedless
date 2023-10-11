import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScrapeSourceComponent } from './scrape-source.component';
import { EmbeddedWebsiteModule } from '../embedded-website/embedded-website.module';
import { FormsModule } from '@angular/forms';
import { IonicModule } from '@ionic/angular';
import { ScrapeActionsModule } from '../scrape-actions/scrape-actions.module';
import { SelectModule } from '../select/select.module';
import { MenuModule } from '../menu/menu.module';
import { SharedModule } from 'primeng/api';
import { SplitterModule } from 'primeng/splitter';
import { InputTextModule } from 'primeng/inputtext';



@NgModule({
  declarations: [ScrapeSourceComponent],
  exports: [ScrapeSourceComponent],
  imports: [
    CommonModule,
    EmbeddedWebsiteModule,
    FormsModule,
    IonicModule,
    ScrapeActionsModule,
    SelectModule,
    MenuModule,
    SharedModule,
    SplitterModule,
    InputTextModule
  ]
})
export class ScrapeSourceModule { }
