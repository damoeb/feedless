import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmbeddedImageComponent } from './embedded-image.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [EmbeddedImageComponent],
  exports: [EmbeddedImageComponent],
  imports: [CommonModule, IonicModule, FormsModule],
})
export class EmbeddedImageModule {}
