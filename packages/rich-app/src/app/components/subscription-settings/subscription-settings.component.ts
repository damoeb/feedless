import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import { isUndefined } from 'lodash';
import { ModalController } from '@ionic/angular';
import { SubscriptionService } from '../../services/subscription.service';
import {
  FieldWrapper,
  GqlBucket,
  GqlNativeFeedRef,
  GqlProxyFeed,
  GqlSubscription,
  Scalars,
} from '../../../generated/graphql';
import { ToastService } from '../../services/toast.service';
import { ChooseFeedUrlComponent } from '../choose-feed-url/choose-feed-url.component';
import { FeedDetailsComponent } from '../feed-details/feed-details.component';

export type FeedRefType = 'native' | 'proxy';

export interface FeedRef {
  type: FeedRefType;
  actualFeed: GqlProxyFeed | GqlNativeFeedRef;
}

@Component({
  selector: 'app-subscription-settings',
  templateUrl: './subscription-settings.component.html',
  styleUrls: ['./subscription-settings.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SubscriptionSettingsComponent implements OnInit {
  @Input()
  subscription: GqlSubscription | null;
  @Input()
  bucket: GqlBucket;

  queryString: string;
  loading: boolean;
  tags: string[] = [];

  throttle: string = '';
  title: string = '';
  titlePlaceholder: string = '';
  feedUrl: string;
  homepageUrl: string;

  constructor(
    private readonly modalController: ModalController,
    private readonly ref: ChangeDetectorRef,
    private readonly subscriptionService: SubscriptionService,
    private readonly changeDetectorRef: ChangeDetectorRef,
    private readonly toastService: ToastService
  ) {}

  ngOnInit() {
    this.refresh();
  }

  existsSubscription(): boolean {
    return !isUndefined(this.subscription);
  }

  async reload() {
    this.subscription = await this.subscriptionService
      .findById(this.subscription.id)
      .toPromise()
      .then((response) => response.data.subscription);
    this.refresh();
  }

  refresh() {
    this.titlePlaceholder =
      this.subscription.feed.title?.length == 0
        ? 'Enter a name'
        : this.subscription.feed.title;
    this.tags = this.subscription?.tags || [];
    this.homepageUrl = this.subscription?.feed?.home_page_url;
    this.queryString = this.subscription?.feed?.feed_url;
    this.feedUrl = this.subscription?.feed?.feed_url;
    this.changeDetectorRef.detectChanges();
  }

  dismissModal() {
    return this.modalController.dismiss();
  }

  async changeFeedUrl(feedUrl: string) {
    console.log('Change feed Url', feedUrl);
    const modal = await this.modalController.create({
      component: ChooseFeedUrlComponent,
      backdropDismiss: false,
      componentProps: {
        query: feedUrl,
      },
    });
    modal.onDidDismiss<GqlNativeFeedRef | GqlProxyFeed>().then((response) => {
      console.log('chose feed', response.data);
      if (response.data) {
        this.feedUrl = response.data.feed_url;
        this.updateSubscription();
        this.changeDetectorRef.detectChanges();
      }
    });

    await modal.present();
  }

  updateSubscription() {
    const { title, id } = this.subscription;
    this.subscriptionService
      .updateSubscription(id, this.feedUrl, this.title || title, this.tags)
      .subscribe(({ data, errors }) => {
        if (errors) {
          this.toastService.errors(errors);
        } else {
          this.toastService.info('Saved');
          this.reload();
        }
      });
  }

  unsubscribe() {
    this.subscriptionService
      .unsubscribe(this.subscription.id)
      .toPromise()
      .then(({ data, errors }) => {
        if (errors) {
          this.toastService.errors(errors);
        } else {
          this.toastService.info('Unsubscribed');
        }
        this.dismissModal();
      });
  }

  getFeedTitle(feedRef: FeedRef) {
    if (feedRef.type === 'native') {
      return (feedRef.actualFeed as GqlNativeFeedRef).title;
    }
    return `Feed with ${
      (feedRef.actualFeed as GqlProxyFeed).articles.length
    } articles`;
  }

  disableSubscription() {
    this.subscriptionService
      .disableById(this.subscription.id)
      .toPromise()
      .then(({ data, errors }) => {
        if (errors) {
          this.toastService.errors(errors);
        } else {
          this.toastService.info('Unsubscribed');
        }
        this.dismissModal();
      });
  }

  async showFeedDetails(feedId: string) {
    const modal = await this.modalController.create({
      component: FeedDetailsComponent,
      backdropDismiss: false,
      componentProps: {
        feedId,
      },
    });
    await modal.present();
  }
}
