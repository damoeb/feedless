import { Component, input } from '@angular/core';
import { NgClass } from '@angular/common';

export type BubbleColor = 'orange' | 'blue' | 'red' | 'gray' | 'green';

@Component({
  selector: 'app-bubble',
  templateUrl: './bubble.component.html',
  styleUrls: ['./bubble.component.scss'],
  imports: [NgClass],
  standalone: true,
})
export class BubbleComponent {
  readonly color = input<BubbleColor>('blue');

  constructor() {}
}
