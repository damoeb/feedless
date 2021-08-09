import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import { GqlNativeFeedRef, GqlProxyFeed } from '../../../generated/graphql';
import { debounce, DebouncedFunc } from 'lodash';
import { ModalController } from '@ionic/angular';
import { SubscriptionService } from '../../services/subscription.service';
import { NativeFeedComponent } from '../native-feed/native-feed.component';
import { GeneratedFeedComponent } from '../generated-feed/generated-feed.component';
import { FeedRef } from '../subscription-settings/subscription-settings.component';

@Component({
  selector: 'app-add-feed-url',
  templateUrl: './choose-feed-url.component.html',
  styleUrls: ['./choose-feed-url.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ChooseFeedUrlComponent implements OnInit {
  @Input()
  query: string = '';
  private searchDebounced: DebouncedFunc<any>;
  loading: boolean;
  resolvedFeedRefs: FeedRef[];

  throttle: string = '';
  title: string = '';
  feedUrl: string;
  private errors = [];
  hasErrors: boolean = false;

  constructor(
    private readonly modalController: ModalController,
    private readonly ref: ChangeDetectorRef,
    private readonly subscriptionService: SubscriptionService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.refresh();
    this.search();
  }

  refresh() {
    this.searchDebounced = debounce(this.search.bind(this), 300);
    this.changeDetectorRef.detectChanges();
  }

  dismissModal() {
    return this.modalController.dismiss();
  }

  search() {
    if (this.query.trim().length < 3) {
      return;
    }
    console.log(`Search ${this.query}`);
    this.resolvedFeedRefs = [];
    this.loading = true;
    this.hasErrors = false;
    this.ref.detectChanges();

    Promise.all([
      this.subscriptionService
        .discoverFeedsByUrl(this.query)
        .toPromise()
        .then(({ data, error }) => {
          this.errors.push(error);
          return data.discoverFeedsByUrl;
        })
        .catch((e) => {
          console.warn(e.message);
          return { nativeFeeds: [], generatedFeeds: {} };
        }),
      this.subscriptionService
        .searchFeeds(this.query)
        .toPromise()
        .then(({ data, error }) => {
          this.errors.push(error);
          return data.feeds;
        })
        .catch((e) => {
          console.warn(e.message);
          return [];
        }),
    ])
      .then((data) => {
        const generatedFeeds =
          data[0].generatedFeeds && data[0].generatedFeeds.feeds
            ? data[0].generatedFeeds.feeds
            : [];
        const nativeFeeds = data[0].nativeFeeds || [];
        this.resolvedFeedRefs = [
          ...nativeFeeds.map((feed: GqlNativeFeedRef) => ({
            type: 'native',
            actualFeed: feed,
          })),
          ...data[1].map((feed: GqlNativeFeedRef) => ({
            type: 'native',
            actualFeed: feed,
          })),
          ...generatedFeeds.map((feed: GqlProxyFeed) => ({
            type: 'proxy',
            actualFeed: feed,
          })),
        ];
        console.log(`${this.resolvedFeedRefs?.length} feeds found`);
      })
      .catch(console.error)
      .finally(() => {
        console.log(`Finalizing`);
        this.hasErrors = this.resolvedFeedRefs?.length === 0;
        if (this.hasErrors) {
          console.log('Error occurred', this.errors);
        }
        this.loading = false;
        this.ref.detectChanges();
      });
  }

  async showFeed(feed: FeedRef) {
    if (feed.type === 'native') {
      return this.showNativeFeed(feed.actualFeed as GqlNativeFeedRef);
    } else {
      return this.showGeneratedFeed(feed.actualFeed as GqlProxyFeed);
    }
  }
  async showNativeFeed(feed: GqlNativeFeedRef) {
    const modal = await this.modalController.create({
      component: NativeFeedComponent,
      backdropDismiss: false,
      componentProps: {
        feed,
        canSubscribe: true,
      },
    });
    modal.onDidDismiss<GqlNativeFeedRef>().then(async (response) => {
      console.log('add native feed', response.data.feed_url);
      if (response.data) {
        setTimeout(() => {
          this.modalController.dismiss(response.data);
        }, 50);
      }
    });

    await modal.present();
  }

  private async showGeneratedFeed(feed: GqlProxyFeed) {
    const modal = await this.modalController.create({
      component: GeneratedFeedComponent,
      backdropDismiss: false,
      componentProps: {
        feed,
      },
    });

    modal.onDidDismiss<GqlProxyFeed>().then(async (response) => {
      if (response.data) {
        console.log('add generated feed', response.data.feed_url);
        setTimeout(() => {
          this.modalController.dismiss(response.data);
        }, 50);
      }
    });

    await modal.present();

    return Promise.resolve(undefined);
  }

  getFeedTitle(feedRef: FeedRef) {
    if (feedRef.type === 'native') {
      return (feedRef.actualFeed as GqlNativeFeedRef).title;
    }
    return `Feed with ${
      (feedRef.actualFeed as GqlProxyFeed).articles.length
    } articles`;
  }
}
