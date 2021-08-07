import { Component, Input, OnInit } from '@angular/core';
import { GqlArticle, GqlArticleRef } from '../../../generated/graphql';

@Component({
  selector: 'app-article',
  templateUrl: './article.component.html',
  styleUrls: ['./article.component.scss'],
})
export class ArticleComponent implements OnInit {
  @Input()
  public articleRef: GqlArticleRef;

  @Input()
  public article: GqlArticle;

  @Input()
  public showTags: boolean;

  @Input()
  public showLink = true;

  constructor() {}

  ngOnInit() {}
}
