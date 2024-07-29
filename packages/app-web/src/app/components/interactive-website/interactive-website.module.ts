import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InteractiveWebsiteComponent } from './interactive-website.component';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { EmbeddedImageModule } from '../embedded-image/embedded-image.module';
import { EmbeddedMarkupModule } from '../embedded-markup/embedded-markup.module';

@NgModule({
  declarations: [InteractiveWebsiteComponent],
  exports: [InteractiveWebsiteComponent],
  imports: [
    CommonModule,
    IonicModule,
    RouterLink,
    FormsModule,
    EmbeddedImageModule,
    EmbeddedMarkupModule,
    ReactiveFormsModule,
  ],
})
export class InteractiveWebsiteModule {}
