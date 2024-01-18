import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { VisualDiffPageRoutingModule } from './visual-diff-routing.module';

import { VisualDiffPage } from './visual-diff.page';
import { ScrapeSourceModule } from '../../components/scrape-source/scrape-source.module';
import { EmbeddedImageModule } from '../../components/embedded-image/embedded-image.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    VisualDiffPageRoutingModule,
    ReactiveFormsModule,
    ScrapeSourceModule,
    EmbeddedImageModule,
    FormsModule,
  ],
  declarations: [VisualDiffPage],
})
export class VisualDiffPageModule {}
