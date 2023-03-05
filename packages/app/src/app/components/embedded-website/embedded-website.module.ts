import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmbeddedWebsiteComponent } from './embedded-website.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [EmbeddedWebsiteComponent],
  exports: [EmbeddedWebsiteComponent],
  imports: [CommonModule, IonicModule],
})
export class EmbeddedWebsiteModule {}
