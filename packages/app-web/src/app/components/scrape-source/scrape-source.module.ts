import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScrapeSourceComponent } from './scrape-source.component';
import { EmbeddedWebsiteModule } from '../embedded-website/embedded-website.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { IonicModule } from '@ionic/angular';
import { MenuModule } from '../../elements/menu/menu.module';
import { EmbeddedImageModule } from '../embedded-image/embedded-image.module';
import { RouterLink } from '@angular/router';
import { SelectModule } from '../../elements/select/select.module';
import { ScrapeSourceStepModule } from '../scrape-source-step/scrape-source-step.module';
import { InputModule } from '../../elements/input/input.module';
import { BubbleModule } from '../bubble/bubble.module';
import { TransformWebsiteToFeedModalModule } from '../../modals/transform-website-to-feed-modal/transform-website-to-feed-modal.module';

@NgModule({
  declarations: [ScrapeSourceComponent],
  exports: [ScrapeSourceComponent],
  imports: [
    CommonModule,
    EmbeddedWebsiteModule,
    FormsModule,
    IonicModule,
    SelectModule,
    MenuModule,
    ReactiveFormsModule,
    EmbeddedImageModule,
    RouterLink,
    ScrapeSourceStepModule,
    TransformWebsiteToFeedModalModule,
    InputModule,
    BubbleModule
  ]
})
export class ScrapeSourceModule {
}
