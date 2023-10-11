import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-feed-builder-card',
  templateUrl: './feed-builder-card.component.html',
  styleUrls: ['./feed-builder-card.component.scss'],
})
export class FeedBuilderCardComponent {

  @Input({required: true})
  title: string;

  @Output()
  dismiss: EventEmitter<any> = new EventEmitter<any>();

  constructor() { }

}
