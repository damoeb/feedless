import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedBuilderModalComponent } from './feed-builder-modal.component';
import { IonicModule } from '@ionic/angular';
import { SelectModule } from '../../components/select/select.module';
import { ScrapeSourceModalModule } from '../scrape-source-modal/scrape-source-modal.module';

@NgModule({
  declarations: [FeedBuilderModalComponent],
  exports: [FeedBuilderModalComponent],
  imports: [
    CommonModule,
    IonicModule,
    SelectModule,
    ScrapeSourceModalModule
  ]
})
export class FeedBuilderModalModule {}
