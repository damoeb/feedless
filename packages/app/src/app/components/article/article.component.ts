import { Component, Input, OnInit } from '@angular/core';
import { GqlArticle, GqlArticleRef } from '../../../generated/graphql';
import { ArticleService } from '../../services/article.service';

interface NamespacedTag {
  color?: string;
  tag: string;
  namespace: string;
}

function getColorByNamespace(type: string): string {
  switch (type) {
    case 'CONTENT':
      return 'success';
    case 'SUBSCRIPTION':
      return 'warning';
  }
}

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
  public linkToReader = true;

  constructor(private readonly articleService: ArticleService) {}

  ngOnInit() {}

  getDomain(): string {
    try {
      return new URL(this.article.url).hostname;
    } catch (e) {
      return '';
    }
  }

  getContent() {
    return this.articleService.removeXmlMetatags(this.article.content_text);
  }

  getTypedTags(tags: NamespacedTag[] = []): NamespacedTag[] {
    return tags
      .map((tag: NamespacedTag) => {
        tag.color = getColorByNamespace(tag.namespace);
        return tag;
      })
      .filter((typedTag) => typedTag);
  }
}
