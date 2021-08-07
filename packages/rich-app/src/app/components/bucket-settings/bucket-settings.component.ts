import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import { ModalController } from '@ionic/angular';
import {
  FieldWrapper,
  GqlBucket,
  GqlNativeFeedRef,
  GqlProxyFeed,
  GqlSubscription,
} from '../../../generated/graphql';
import { SubscriptionSettingsComponent } from '../subscription-settings/subscription-settings.component';
import { BucketService } from '../../services/bucket.service';
import { BubbleColor } from '../bubble/bubble.component';
import { ChooseFeedUrlComponent } from '../choose-feed-url/choose-feed-url.component';
import { SubscriptionService } from '../../services/subscription.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-bucket-settings',
  templateUrl: './bucket-settings.component.html',
  styleUrls: ['./bucket-settings.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BucketSettingsComponent implements OnInit {
  @Input()
  bucket: GqlBucket;
  accordion = {
    filters: 0,
    postProcessors: 1,
    throttle: 3,
  };
  currentAccordion: number;

  constructor(
    private readonly modalController: ModalController,
    private readonly bucketService: BucketService,
    private readonly subscriptionService: SubscriptionService,
    private readonly toastService: ToastService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {}

  ngOnInit() {}

  async dismissModal() {
    await this.modalController.dismiss();
  }

  async addSubscription() {
    const modal = await this.modalController.create({
      component: ChooseFeedUrlComponent,
      backdropDismiss: false,
    });

    await modal.present();
    const response = await modal.onDidDismiss<
      GqlProxyFeed | GqlNativeFeedRef
    >();

    if (response.data) {
      this.subscriptionService
        .createSubscription(response.data.feed_url, this.bucket.id)
        .toPromise()
        .then(({ data, errors }) => {
          if (errors) {
            this.toastService.errors(errors);
          } else {
            this.toastService.info('Subscribed');
          }
        });
    }
  }

  private refreshBucketData() {
    this.bucketService
      .getBucketsById(this.bucket.id)
      .subscribe(({ data, error }) => {
        console.log('update bucket', data);
        this.bucket = data.bucket;
        this.changeDetectorRef.detectChanges();
      });
  }

  async openSubscriptionModal(subscription?: GqlSubscription) {
    const modal = await this.modalController.create({
      component: SubscriptionSettingsComponent,
      backdropDismiss: false,
      componentProps: {
        subscription,
        bucket: this.bucket,
      },
    });

    await modal.present();
    modal.onDidDismiss().then(({ data }) => {
      this.refreshBucketData();
    });
  }

  editSubscription(subscription: GqlSubscription) {
    return this.openSubscriptionModal(subscription);
  }

  toggle(accordion: number) {
    if (this.currentAccordion === accordion) {
      this.currentAccordion = null;
    } else {
      this.currentAccordion = accordion;
    }
  }

  isActive(accordeon: number) {
    return this.currentAccordion === accordeon;
  }

  isHealthy(subscription: GqlSubscription): boolean {
    return subscription?.feed?.status === 'ok';
  }

  addPostProcessor() {}

  getSubscriptionBubbleColor(
    subscription: FieldWrapper<GqlSubscription>
  ): BubbleColor {
    if (subscription.feed?.broken) {
      if (subscription.feed?.inactive) {
        return 'gray';
      } else {
        return 'red';
      }
    } else {
      return 'green';
    }
  }

  save() {
    // todo mag save
    return this.modalController.dismiss();
  }

  getSubscriptionsCount(): string {
    let activeCount = this.bucket.subscriptions.filter(
      (s) => !s.feed.broken
    ).length;

    if (activeCount !== this.bucket.subscriptions.length) {
      return `( ${activeCount} active of ${this.bucket.subscriptions.length} )`;
    }
  }

  deleteBucket() {
    this.bucketService.delteById(this.bucket.id);
    return this.modalController.dismiss();
  }

  getTagsForSubscription(
    subscription: FieldWrapper<GqlSubscription>
  ): string[] {
    try {
      return JSON.parse(subscription.tags);
    } catch (e) {
      return [];
    }
  }
}
