import { Component, Input } from '@angular/core';

export type BubbleColor = 'blue' | 'red' | 'gray' | 'green';

@Component({
  selector: 'app-bubble',
  templateUrl: './bubble.component.html',
  styleUrls: ['./bubble.component.scss']
})
export class BubbleComponent {
  @Input()
  color: BubbleColor = 'blue';

  constructor() {
  }
}
