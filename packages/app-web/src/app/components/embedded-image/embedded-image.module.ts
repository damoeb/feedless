import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmbeddedImageComponent } from './embedded-image.component';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [EmbeddedImageComponent],
  exports: [EmbeddedImageComponent],
  imports: [CommonModule, FormsModule],
})
export class EmbeddedImageModule {}
