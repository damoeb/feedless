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
import { FeedService, NativeFeed } from '../../services/feed.service';
import { without } from 'lodash';
import {
  ActionSheetController,
  InfiniteScrollCustomEvent,
} from '@ionic/angular';
import { Pagination } from '../../services/pagination.service';

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
  articles: Article[] = [];
  checkedArticles: Article[] = [];
  pagination: Pagination;
  private currentPage = 0;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly articleService: ArticleService,
    private readonly actionSheetCtrl: ActionSheetController,
    private readonly feedService: FeedService
  ) {}

  ngOnInit() {
    console.log(this.id);
    this.initFeed(this.id);
  }

  onCheckChange(event: any, article: Article) {
    if (event.detail.checked) {
      this.checkedArticles.push(article);
    } else {
      this.checkedArticles = without(this.checkedArticles, article);
    }
  }

  isChecked(article: Article): boolean {
    return this.checkedArticles.indexOf(article) > -1;
  }

  toggleCheckAll(event: any) {
    if (event.detail.checked) {
      this.checkedArticles = [...this.articles];
    } else {
      this.checkedArticles = [];
    }
  }

  async showActions() {
    const actionSheet = await this.actionSheetCtrl.create({
      header: `Actions for ${this.checkedArticles.length} Articles`,
      buttons: [
        {
          text: 'Forward',
          role: 'destructive',
          handler: () => {
            // todo mag
          },
        },
        {
          text: 'Trigger Plugin',
          role: 'destructive',
          handler: () => {
            // todo mag
          },
        },
      ],
    });

    await actionSheet.present();

    const result = await actionSheet.onDidDismiss();
  }

  getFeedUrl(): string {
    return `/feed:${this.feed.id}/atom`;
  }

  async loadMoreArticles(event: InfiniteScrollCustomEvent) {
    if (!this.pagination.isLast) {
      this.currentPage++;
      await this.fetchArticles();
      await event.target.complete();
    }
  }

  private async initFeed(feedId: string) {
    this.loading = true;
    try {
      this.feed = await this.feedService.getNativeFeed({
        where: {
          id: feedId
        }
      });
      this.feedName.emit(this.feed.title);
    } finally {
      this.loading = false;
    }
    this.changeRef.detectChanges();

    this.fetchArticles();
  }

  private async fetchArticles() {
    const response = await this.articleService.findAllByStreamId(
      this.feed.streamId,
      this.currentPage
    );
    this.articles.push(...response.articles);
    this.pagination = response.pagination;
    this.changeRef.detectChanges();
  }
}
