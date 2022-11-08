import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { Article, Content, Enclosure, ArticleService } from '../../services/article.service';
import { NativeFeed, FeedService } from '../../services/feed.service';

@Component({
  selector: 'app-article',
  templateUrl: './article.component.html',
  styleUrls: ['./article.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ArticleComponent implements OnInit {

  @Input()
  articleId: string;
  @Input()
  feedId: string;
  @Input()
  oneline: boolean;
  @Input()
  renderFulltext: boolean;
  audioStreams: Enclosure[] = [];
  videoStreams: Enclosure[] = [];

  article: Article;
  feed: NativeFeed;
  content: Content;

  constructor(private readonly articleService: ArticleService,
              private readonly changeRef: ChangeDetectorRef,
              private readonly feedService: FeedService) { }

  async ngOnInit() {
    this.article = await this.articleService.findById(this.articleId)
    this.feed = await this.feedService.getNativeFeedById(this.feedId)

    const content = this.article.content;
    this.content = content;

    if (content.enclosures) {
      this.audioStreams = content.enclosures.filter(enclosure => enclosure.type.startsWith('audio'))
      this.videoStreams = content.enclosures.filter(enclosure => enclosure.type.startsWith('video'))
    }
    this.changeRef.detectChanges();
  }

  createdAt(): Date {
    return new Date(this.content.publishedAt)
  }

  trimToFallback(actualValue: string, fallback: string): string {
    if (actualValue && actualValue.trim().length > 0) {
      return actualValue;
    }
    return fallback;
  }
}
