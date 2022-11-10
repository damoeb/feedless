import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import {
  Article,
  ArticleService,
  Content,
  Enclosure,
} from '../../services/article.service';
import { FeedService, NativeFeed } from '../../services/feed.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-article',
  templateUrl: './article.component.html',
  styleUrls: ['./article.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ArticleComponent implements OnInit {
  @Input()
  article: Article;
  @Input()
  renderFulltext: boolean;
  @Input()
  urlPrefix: string;

  audioStreams: Enclosure[] = [];
  videoStreams: Enclosure[] = [];

  feed: NativeFeed;
  content: Content;
  bucketId: string;

  constructor(
    private readonly articleService: ArticleService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly activatedRoute: ActivatedRoute,
    private readonly feedService: FeedService
  ) {}

  async ngOnInit() {
    this.feed = await this.feedService.getNativeFeedById(this.article.nativeFeedId);

    const content = this.article.content;
    this.content = content;

    if (content.enclosures) {
      this.audioStreams = content.enclosures.filter((enclosure) =>
        enclosure.type.startsWith('audio')
      );
      this.videoStreams = content.enclosures.filter((enclosure) =>
        enclosure.type.startsWith('video')
      );
    }
    this.changeRef.detectChanges();
  }

  createdAt(): Date {
    return new Date(this.content.publishedAt);
  }

  trimToFallback(actualValue: string, fallback: string): string {
    if (actualValue && actualValue.trim().length > 0) {
      return actualValue;
    }
    return fallback;
  }
}
