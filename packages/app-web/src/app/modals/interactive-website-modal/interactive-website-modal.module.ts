import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';
import { InteractiveWebsiteModalComponent } from './interactive-website-modal.component';
import { EmbeddedImageModule } from '../../components/embedded-image/embedded-image.module';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { InteractiveWebsiteModule } from '../../components/interactive-website/interactive-website.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    ReactiveFormsModule,
    EmbeddedImageModule,
    SearchbarModule,
    InteractiveWebsiteModule,
  ],
  declarations: [InteractiveWebsiteModalComponent],
  exports: [InteractiveWebsiteModalComponent],
})
export class InteractiveWebsiteModalModule {}
