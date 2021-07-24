import { Component, Input, OnInit } from '@angular/core';
import { GqlArticle } from '../../../generated/graphql';

@Component({
  selector: 'app-feed-item',
  templateUrl: './feed-item.component.html',
  styleUrls: ['./feed-item.component.scss'],
})
export class FeedItemComponent implements OnInit {
  @Input()
  public article: GqlArticle;

  constructor() {}

  ngOnInit() {}
}
