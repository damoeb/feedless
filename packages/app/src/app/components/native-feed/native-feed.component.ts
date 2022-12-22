import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { Article, ArticleService } from '../../services/article.service';
import { BasicNativeFeed, FeedService, NativeFeed } from '../../services/feed.service';

@Component({
  selector: 'app-native-feed',
  templateUrl: './native-feed.component.html',
  styleUrls: ['./native-feed.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NativeFeedComponent implements OnInit {
  @Input()
  id: string;

  @Output()
  feedName: EventEmitter<string> = new EventEmitter<string>();

  loading: boolean;
  feed: NativeFeed;
  articles: Article[];
  private currentPage = 0;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly articleService: ArticleService,
    private readonly feedService: FeedService
  ) {}

  ngOnInit() {
    console.log(this.id);
    this.initFeed(this.id);
  }

  private async initFeed(feedId: string) {
    this.loading = true;
    try {
      this.feed = await this.feedService.getNativeFeedById(feedId);
      this.feedName.emit(this.feed.title);
    } finally {
      this.loading = false;
    }
    this.changeRef.detectChanges();

    this.fetchArticles();
  }

  private async fetchArticles() {
    const response = await this.articleService.findAllByStreamId(this.feed.streamId, this.currentPage);
    this.articles = response.articles;
    // this.pagination = response.pagination;
    this.changeRef.detectChanges();
  }
}
