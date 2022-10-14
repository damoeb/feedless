import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ArticleService } from '../../services/article.service';
import { ActualBucket, FeedService } from '../../services/feed.service';

@Component({
  selector: 'app-native-feed',
  templateUrl: './native-feed.component.html',
  styleUrls: ['./native-feed.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NativeFeedComponent implements OnInit {

  @Input()
  id: string;

  @Output()
  feedName: EventEmitter<string> = new EventEmitter<string>();

  loadingFeed: boolean;
  feed: ActualBucket;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly articleService: ArticleService,
    private readonly feedService: FeedService) { }

  ngOnInit() {}

  private async initFeed(feedId: string) {
    console.log('initFeed', feedId)
    this.loadingFeed = true;
    try {
      this.feed = await this.feedService.getNativeFeedById(feedId);
      this.feedName.emit(this.feed.name);
    } finally {
      this.loadingFeed = false;
    }
    console.log('this.feed', this.feed)
    this.changeRef.detectChanges();

    this.fetchArticles();
  }

  private async fetchArticles() {
    let response = await this.articleService.findAllByStreamId(this.feed.streamId, this.currentPage);
    this.articles = response.articles;
    this.pagination = response.pagination;
    this.changeRef.detectChanges();
    // this.articles = response.articles;
  }


}
