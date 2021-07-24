import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import { debounce, DebouncedFunc, isUndefined } from 'lodash';
import { ModalController } from '@ionic/angular';
import { SubscriptionService } from '../../services/subscription.service';
import {
  GqlBucket,
  GqlDiscoveredFeed,
  GqlSubscription,
} from '../../../generated/graphql';
import { FeedComponent } from '../feed/feed.component';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-add-subscription',
  templateUrl: './add-subscription.component.html',
  styleUrls: ['./add-subscription.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AddSubscriptionComponent implements OnInit {
  @Input()
  subscription: GqlSubscription | null;
  @Input()
  bucket: GqlBucket;

  queryString: string;
  private searchDebounced: DebouncedFunc<any>;
  loading: boolean;
  editMode: boolean;
  tags: string;
  resolvedFeeds: GqlDiscoveredFeed[];
  throttle: string = '';

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

  refresh() {
    this.tags = this.subscription?.tags;
    this.editMode = !isUndefined(this.subscription);
    this.searchDebounced = debounce(this.search.bind(this), 300);
    this.queryString = this.subscription?.feed?.feed_url;
    this.changeDetectorRef.detectChanges();
  }

  dismissModal(type: string) {
    return this.modalController.dismiss(type);
  }

  search() {
    if (this.editMode) {
      return;
    }
    this.loading = true;
    const sub = this.subscriptionService
      .discoverFeeds(this.queryString)
      .subscribe(({ data, error, loading }) => {
        if (loading) {
        } else if (error) {
          this.toastService.errorFromApollo(error);
        } else {
          this.resolvedFeeds = data.discoverFeedsByQuery;
          this.loading = false;
          this.ref.detectChanges();
          sub.unsubscribe();
        }
      });
  }

  updateSubscription() {
    this.subscriptionService
      .updateSubscription(this.subscription, this.tags)
      .subscribe(({ data, errors }) => {
        if (errors) {
          this.toastService.errors(errors);
        } else {
          this.toastService.info('Saved');
          return this.dismissModal('update');
        }
      });
  }

  async showFeed(feed: GqlDiscoveredFeed) {
    const modal = await this.modalController.create({
      component: FeedComponent,
      componentProps: {
        feed,
      },
    });
    modal.onDidDismiss<GqlDiscoveredFeed>().then((response) => {
      console.log('chose feed', response.data);
      if (response.data) {
        this.subscriptionService
          .createSubscription(response.data.url, this.bucket.id)
          .toPromise()
          .then(({ data, errors }) => {
            if (errors) {
              this.toastService.errors(errors);
            } else {
              this.toastService.info('Subscribed');

              this.subscriptionService
                .findById(data.subscribeToFeed.id)
                .toPromise()
                .then(({ data }) => {
                  this.subscription = data.subscription;
                  this.resolvedFeeds = [];
                  this.refresh();
                });
            }
          });
      }
    });

    await modal.present();
  }

  isChosenFeed(feed: GqlDiscoveredFeed) {
    return false;
  }

  unsubscribe() {
    this.subscriptionService
      .unsubscribe(this.subscription.id)
      .subscribe(({ data, errors }) => {
        if (errors) {
          this.toastService.errors(errors);
        } else {
          this.toastService.info('Unsubscribed');
        }
        this.dismissModal('unsubscribe');
      });
  }

  getHomepageUrl() {
    return this.subscription?.feed?.home_page_url;
  }

  preview() {}
}
