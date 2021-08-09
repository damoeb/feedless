import { Component, Input, OnInit } from '@angular/core';
import { GqlArticle, GqlArticleRef } from '../../../generated/graphql';
import { ArticleService } from '../../services/article.service';

interface TypedTag {
  color: string;
  name: string;
}

function getColorByType(type: string): string {
  return 'warning';
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
  public showLink = true;

  constructor(private readonly articleService: ArticleService) {}

  ngOnInit() {}

  getDomain(): string {
    return new URL(this.article.url).hostname;
  }

  getContent() {
    return this.articleService.removeXmlMetatags(this.article.content_text);
  }

  getTypedTags(): TypedTag[] {
    return (this.articleRef.tags || [])
      .map((tag) => {
        const [type, name] = tag.split(':');
        return {
          color: getColorByType(type),
          name,
        };
      })
      .filter((typedTag) => typedTag);
  }
}
