import { Component, Input, OnInit } from '@angular/core';

export type BubbleColor = 'blue' | 'red' | 'gray' | 'green';

@Component({
  selector: 'app-bubble',
  templateUrl: './bubble.component.html',
  styleUrls: ['./bubble.component.scss'],
})
export class BubbleComponent implements OnInit {
  @Input()
  color: BubbleColor = 'blue';

  constructor() {}

  ngOnInit() {}
}
