import { Component, Input, OnInit } from '@angular/core';
import { GqlArticleRef } from '../../../generated/graphql';

@Component({
  selector: 'app-article',
  templateUrl: './article.component.html',
  styleUrls: ['./article.component.scss'],
})
export class ArticleComponent implements OnInit {
  @Input()
  public articleRef: GqlArticleRef;

  constructor() {}

  ngOnInit() {}
}
