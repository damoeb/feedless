import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { ActualArticle, ActualEnclosure, ArticleService } from '../../services/article.service';
import { ActualNativeFeed, FeedService } from '../../services/feed.service';

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
  audioStreams: ActualEnclosure[] = [];
  videoStreams: ActualEnclosure[] = [];

  article: ActualArticle;
  feed: ActualNativeFeed;

  constructor(private readonly articleService: ArticleService,
              private readonly changeRef: ChangeDetectorRef,
              private readonly feedService: FeedService) { }

  async ngOnInit() {
    this.article = await this.articleService.findById(this.articleId)
    this.feed = await this.feedService.getNativeFeedById(this.feedId)

    if (this.article.enclosures) {
      this.audioStreams = this.article.enclosures.filter(enclosure => enclosure.type.startsWith('audio'))
      this.videoStreams = this.article.enclosures.filter(enclosure => enclosure.type.startsWith('video'))
    }
    this.changeRef.detectChanges();
  }

  createdAt(): Date {
    return new Date(this.article.publishedAt)
  }

  trimToFallback(actualValue: string, fallback: string): string {
    if (actualValue && actualValue.trim().length > 0) {
      return actualValue;
    }
    return fallback;
  }
}
