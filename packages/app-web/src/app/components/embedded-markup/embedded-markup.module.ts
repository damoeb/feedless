import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmbeddedMarkupComponent } from './embedded-markup.component';
import { RouterLink } from '@angular/router';

@NgModule({
  declarations: [EmbeddedMarkupComponent],
  exports: [EmbeddedMarkupComponent],
  imports: [CommonModule, RouterLink],
})
export class EmbeddedMarkupModule {}
