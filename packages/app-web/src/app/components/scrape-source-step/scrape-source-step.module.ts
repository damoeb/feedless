import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScrapeSourceStepComponent } from './scrape-source-step.component';
import { EmbeddedWebsiteModule } from '../embedded-website/embedded-website.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { IonicModule } from '@ionic/angular';
import { SelectModule } from '../select/select.module';
import { MenuModule } from '../menu/menu.module';
import { EmbeddedImageModule } from '../embedded-image/embedded-image.module';
import { RouterLink } from '@angular/router';
import { Select2Module } from '../select2/select2.module';


@NgModule({
  declarations: [ScrapeSourceStepComponent],
  exports: [ScrapeSourceStepComponent],
  imports: [
    CommonModule,
    EmbeddedWebsiteModule,
    FormsModule,
    IonicModule,
    SelectModule,
    Select2Module,
    MenuModule,
    ReactiveFormsModule,
    EmbeddedImageModule,
    RouterLink
  ]
})
export class ScrapeSourceStepModule { }
