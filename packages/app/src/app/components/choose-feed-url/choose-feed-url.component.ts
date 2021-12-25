import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { GqlGenericFeedRule, GqlNativeFeedRef } from '../../../generated/graphql';
import { debounce, DebouncedFunc } from 'lodash';
import { ModalController } from '@ionic/angular';
import { SubscriptionService } from '../../services/subscription.service';
import { NativeFeedComponent } from '../native-feed/native-feed.component';
import { GeneratedFeedComponent } from '../generated-feed/generated-feed.component';
import { FeedRef } from '../subscription-settings/subscription-settings.component';
import { InspectionComponent } from '../inspection/inspection.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-add-feed-url',
  templateUrl: './choose-feed-url.component.html',
  styleUrls: ['./choose-feed-url.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ChooseFeedUrlComponent implements OnInit {
  @Input()
  query = '';
  private searchDebounced: DebouncedFunc<any>;
  loading: boolean;
  resolvedFeedRefs: FeedRef[];

  throttle = '';
  title = '';
  feedUrl: string;
  private errors = [];
  hasErrors = false;

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

  async search() {
    if (this.loading || this.query.trim().length < 3) {
      return;
    }
    console.log(`Search ${this.query}`);
    this.resolvedFeedRefs = [];
    this.loading = true;
    this.hasErrors = false;
    this.ref.detectChanges();

    await firstValueFrom(this.subscriptionService
        .discoverFeedsByUrl(this.query)
        )
        // .then(({ data, error }) => {
        //   this.errors.push(error);
        //   return data.discoverFeedsByUrl;
        // })
        // .catch((e) => {
        //   console.warn(e.message);
        //   return { nativeFeeds: [], generatedFeeds: {} };
        // }),
      .then((response) => response.data.discoverFeedsByUrl)
      .then((data) => {
        const generatedFeeds = data.genericFeedRules || [];
        const nativeFeeds = data.nativeFeeds || [];
        this.resolvedFeedRefs = [
          ...nativeFeeds.map((feed: GqlNativeFeedRef) => ({
            type: 'native',
            actualFeed: feed,
          })),
          ...generatedFeeds.map((feed: GqlGenericFeedRule) => ({
            type: 'proxy',
            actualFeed: feed,
          })),
        ];
        console.log(`${this.resolvedFeedRefs?.length} feeds found`);
      })
      .catch(console.error)
      .finally(() => {
        console.log(`Finalizing`);
        // this.hasErrors = this.resolvedFeedRefs?.length === 0;
        // if (this.hasErrors) {
        //   console.log('Error occurred', this.errors);
        // }
        this.loading = false;
        this.ref.detectChanges();
      });
  }

  async showFeed(feed: FeedRef) {
    if (feed.type === 'native') {
      return this.showNativeFeed(feed.actualFeed as GqlNativeFeedRef);
    } else {
      return this.showGeneratedFeed(feed.actualFeed as GqlGenericFeedRule);
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

  private async showGeneratedFeed(feed: GqlGenericFeedRule) {
    const modal = await this.modalController.create({
      component: GeneratedFeedComponent,
      backdropDismiss: false,
      componentProps: {
        feed,
      },
    });

    modal.onDidDismiss<GqlGenericFeedRule>().then(async (response) => {
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
      (feedRef.actualFeed as GqlGenericFeedRule).count
    } articles`;
  }

  hasNoNativeFeeds() {
    return this.resolvedFeedRefs?.every((feed) => feed.type === 'proxy');
  }

  async showPageInspection(url: string) {
    const modal = await this.modalController.create({
      component: PageInspectionComponent,
      backdropDismiss: false,
      componentProps: {
        url,
      },
    });
    // modal.onDidDismiss<GqlNativeFeedRef>().then(async (response) => {
    //   console.log('add native feed', response.data.feed_url);
    //   if (response.data) {
    //     setTimeout(() => {
    //       this.modalController.dismiss(response.data);
    //     }, 50);
    //   }
    // });

    await modal.present();
  }
}
