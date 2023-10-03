import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { VisualDiffPageRoutingModule } from './visual-diff-routing.module';

import { VisualDiffPage } from './visual-diff.page';
import { EmbeddedWebsiteModule } from '../../components/embedded-website/embedded-website.module';
import { ReaderModule } from '../../components/reader/reader.module';
import { ScrapeActionsModule } from '../../components/scrape-actions/scrape-actions.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    VisualDiffPageRoutingModule,
    EmbeddedWebsiteModule,
    ReaderModule,
    ScrapeActionsModule,
    ReactiveFormsModule,
  ],
  declarations: [VisualDiffPage],
})
export class VisualDiffPageModule {}
