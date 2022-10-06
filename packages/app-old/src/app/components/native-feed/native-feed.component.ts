import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import { ModalController } from '@ionic/angular';

import {
  GqlArticle,
  GqlArticleRef,
  GqlFeed,
  GqlNativeFeedRef,
} from '../../../generated/graphql';
import { ArticleService } from '../../services/article.service';
import { FeedService } from '../../services/feed.service';

@Component({
  selector: 'app-native-feed',
  templateUrl: './native-feed.component.html',
  styleUrls: ['./native-feed.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NativeFeedComponent implements OnInit {
  @Input()
  feed: GqlNativeFeedRef;
  @Input()
  canSubscribe = false;
  loading = false;
  articleRefs: GqlArticleRef[] = [];

  constructor(
    private readonly modalController: ModalController,
    private readonly changeDetectorRef: ChangeDetectorRef,
    private readonly articleService: ArticleService,
    private readonly feedService: FeedService
  ) {}

  ngOnInit() {
    this.loading = true;
    Promise.all([
      this.feedService
        .metadataForNativeFeed(this.feed.feed_url)
        .toPromise()
        .then((response: any) => {
          const feed: GqlFeed = response.data.metadataForNativeFeedByUrl;
          this.feed.home_page_url = feed.home_page_url;
          this.feed.title = feed.title;
        }),
      this.articleService
        .getArticlesForNativeFeed(this.feed.feed_url)
        .toPromise()
        .then((response: any) => {
          this.articleRefs = response.data.articlesForFeedUrl.map(
            (article: GqlArticle) => {
              return {
                article,
              };
            }
          );
        }),
    ]).then(() => {
      this.loading = false;
      this.changeDetectorRef.detectChanges();
    });
  }
  // metadataForNativeFeedByUrl

  dismissModal() {
    return this.modalController.dismiss();
  }

  async subscribe() {
    return this.modalController.dismiss(this.feed);
  }
}
