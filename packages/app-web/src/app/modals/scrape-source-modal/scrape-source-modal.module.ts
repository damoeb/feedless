import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScrapeSourceModalComponent } from './scrape-source-modal.component';
import { IonicModule } from '@ionic/angular';
import { ScrapeSourceModule } from '../../components/scrape-source/scrape-source.module';

@NgModule({
  declarations: [ScrapeSourceModalComponent],
  exports: [ScrapeSourceModalComponent],
  imports: [
    CommonModule,
    IonicModule,
    ScrapeSourceModule
  ]
})
export class ScrapeSourceModalModule {}
