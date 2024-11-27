import { Component, Input } from '@angular/core';

export type BubbleColor = 'orange' | 'blue' | 'red' | 'gray' | 'green';

@Component({
    selector: 'app-bubble',
    templateUrl: './bubble.component.html',
    styleUrls: ['./bubble.component.scss'],
    standalone: false
})
export class BubbleComponent {
  @Input()
  color: BubbleColor = 'blue';

  constructor() {}
}
