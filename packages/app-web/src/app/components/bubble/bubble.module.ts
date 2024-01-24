import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BubbleComponent } from './bubble.component';

@NgModule({
  declarations: [BubbleComponent],
  exports: [BubbleComponent],
  imports: [CommonModule]
})
export class BubbleModule {
}
