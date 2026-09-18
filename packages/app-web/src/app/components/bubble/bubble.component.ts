import { Component, input, ChangeDetectionStrategy } from '@angular/core';
import { NgClass } from '@angular/common';

export type BubbleColor = 'orange' | 'blue' | 'red' | 'gray' | 'green';

@Component({
  selector: 'app-bubble',
  templateUrl: './bubble.component.html',
  styleUrls: ['./bubble.component.scss'],
  imports: [NgClass],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: true,
})
export class BubbleComponent {
  readonly color = input<BubbleColor>('blue');
}
