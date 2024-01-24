import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScrapeSourceStepComponent } from './scrape-source-step.component';
import { EmbeddedWebsiteModule } from '../embedded-website/embedded-website.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { IonicModule } from '@ionic/angular';
import { MenuModule } from '../../elements/menu/menu.module';
import { EmbeddedImageModule } from '../embedded-image/embedded-image.module';
import { RouterLink } from '@angular/router';
import { SelectModule } from '../../elements/select/select.module';

@NgModule({
  declarations: [ScrapeSourceStepComponent],
  exports: [ScrapeSourceStepComponent],
  imports: [
    CommonModule,
    EmbeddedWebsiteModule,
    FormsModule,
    IonicModule,
    SelectModule,
    MenuModule,
    ReactiveFormsModule,
    EmbeddedImageModule,
    RouterLink
  ]
})
export class ScrapeSourceStepModule {
}
