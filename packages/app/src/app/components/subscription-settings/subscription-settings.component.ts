import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { SubscriptionService } from '../../services/subscription.service';
import { GqlBucket, GqlFeed, GqlGenericFeedRule, GqlNativeFeedRef, GqlSubscription } from '../../../generated/graphql';
import { ToastService } from '../../services/toast.service';
import { ChooseFeedUrlComponent } from '../choose-feed-url/choose-feed-url.component';
import { FeedDetailsComponent } from '../feed-details/feed-details.component';
import { firstValueFrom } from 'rxjs';

export interface FeedRef {
  type: 'native' | 'proxy';
  actualFeed: GqlGenericFeedRule | GqlNativeFeedRef;
}

@Component({
  selector: 'app-subscription-settings',
  templateUrl: './subscription-settings.component.html',
  styleUrls: ['./subscription-settings.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SubscriptionSettingsComponent implements OnInit {
  constructor(
    private readonly modalController: ModalController,
    private readonly ref: ChangeDetectorRef,
    private readonly subscriptionService: SubscriptionService,
    private readonly changeDetectorRef: ChangeDetectorRef,
    private readonly toastService: ToastService
  ) {}
  @Input()
  subscription: GqlSubscription | null;
  @Input()
  bucket: GqlBucket;

  queryString: string;
  loading: boolean;
  stringOfTags = '';

  throttle = '';
  titlePlaceholder = '';
  changed = false;
  private originalFeedUrl: string;

  private tagsToString(tags: string[] = []): string {
    try {
      return tags.join(', ');
    } catch (e) {
      console.error('Cannot parse tags', tags, e.message);
    }
    return '';
  }

  ngOnInit() {
    this.reload();
  }

  async reload() {
    this.subscription = await firstValueFrom(this.subscriptionService
      .findById(this.subscription.id)
    )
      .then((response) => response.data.subscription as GqlSubscription);
    this.originalFeedUrl = this.subscription.feed.feed_url;
    this.titlePlaceholder =
      this.subscription.feed.title?.length === 0
        ? 'Enter a name'
        : `${this.subscription.feed.title} (overwrite, defaults to feed title)`;
    this.stringOfTags = '';
    // this.tagsToString(
    //   this.subscription?.tags
    // );
    this.queryString = this.subscription?.feed?.feed_url;
    this.changeDetectorRef.detectChanges();
  }

  dismissModal() {
    return this.modalController.dismiss(this.changed);
  }

  async changeFeedUrl() {
    const feedUrl = this.subscription?.feed?.feed_url;
    console.log('Change feed Url', feedUrl);
    console.log('open ChooseFeedUrlComponent');
    const modal = await this.modalController.create({
      component: ChooseFeedUrlComponent,
      backdropDismiss: false,
      componentProps: {
        query: feedUrl,
      },
    });
    modal
      .onDidDismiss<GqlNativeFeedRef | GqlGenericFeedRule>()
      .then((response) => {
        console.log('change feed', response.data);
        if (response.data) {
          this.subscription.feed.feed_url = response.data.feed_url;
          // this.subscription.feed.home_page_url = response.data.home_page_url;
          // this.subscription.feed.title = response.data.title;
          this.subscription.feed.broken = false;
          // this.subscription.title = response.data.title;
          this.changed = true;
          this.changeDetectorRef.detectChanges();
        }
      });

    await modal.present();
  }

  updateSubscription() {
    this.subscriptionService
      .updateSubscription(
        this.subscription,
        this.subscription.feed,
        this.tagsFromString()
      )
      .subscribe(({ errors }) => {
        if (errors) {
          this.toastService.errors(errors);
        } else {
          this.toastService.info('Saved');
          this.dismissModal();
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

  disableSubscription(disable: boolean = true) {
    this.subscriptionService
      .disableById(this.subscription.id, disable)
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

  async showFeedDetails(feed: GqlFeed) {
    console.log('open FeedDetailsComponent');
    const modal = await this.modalController.create({
      component: FeedDetailsComponent,
      backdropDismiss: false,
      componentProps: {
        feed,
      },
    });
    await modal.present();
  }

  tagsFromString(): string[] {
    return this.stringOfTags
      .trim()
      .split(/[,;]/)
      .filter((tag, index) => index < 3 && tag.trim().length > 0);
  }

  hasChangedFeedUrl() {
    return this.subscription.feed.feed_url !== this.originalFeedUrl;
  }

  hasBrokenFeedUrl() {
    return this.subscription.feed.broken;
  }
}
