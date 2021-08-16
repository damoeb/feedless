import { Component, Input, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';
import * as timeago from 'timeago.js';
import { ChooseFeedUrlComponent } from '../choose-feed-url/choose-feed-url.component';
import {
  FieldWrapper,
  GqlBucket,
  GqlNativeFeedRef,
  GqlProxyFeed,
  GqlSubscription,
} from '../../../generated/graphql';
import { SubscriptionService } from '../../services/subscription.service';
import { ToastService } from '../../services/toast.service';
import { SubscriptionSettingsComponent } from '../subscription-settings/subscription-settings.component';
import { BubbleColor } from '../bubble/bubble.component';

@Component({
  selector: 'app-subscriptions',
  templateUrl: './subscriptions.component.html',
  styleUrls: ['./subscriptions.component.scss'],
})
export class SubscriptionsComponent implements OnInit {
  @Input()
  bucket: GqlBucket;

  subscriptions: GqlSubscription[];
  private changed: boolean = false;

  constructor(
    private readonly modalController: ModalController,
    private readonly subscriptionService: SubscriptionService,
    private readonly toastService: ToastService
  ) {}

  ngOnInit() {
    this.reload();
  }

  async reload() {
    this.subscriptions = await this.subscriptionService
      .findAllByBucket(this.bucket.id)
      .toPromise()
      .then(({ data, errors }) => {
        if (errors) {
          this.toastService.errors(errors);
        }
        return data.subscriptions;
      });
  }

  dismissModal() {
    return this.modalController.dismiss(this.changed);
  }

  async addSubscription() {
    console.log('addSubscription');
    const modal = await this.modalController.create({
      component: ChooseFeedUrlComponent,
    });

    await modal.present();
    const response = await modal.onDidDismiss<
      GqlProxyFeed | GqlNativeFeedRef
    >();

    if (response.data) {
      this.subscriptionService
        .createSubscription(response.data, this.bucket.id)
        .toPromise()
        .then(({ data, errors }) => {
          if (errors) {
            this.toastService.errors(errors);
          } else {
            this.toastService.info('Subscribed');
            this.changed = true;
            this.reload();
          }
        });
    }
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

  async editSubscription(subscription?: GqlSubscription) {
    console.log('editSubscription');
    const modal = await this.modalController.create({
      component: SubscriptionSettingsComponent,
      backdropDismiss: false,
      componentProps: {
        subscription,
        bucket: this.bucket,
      },
    });

    await modal.present();
    const response = await modal.onDidDismiss<boolean>();
    if (response.data) {
      this.changed = true;
      await this.reload();
    }
  }

  getLastUpdatedAt(subscription: GqlSubscription) {
    return timeago.format(subscription.lastUpdatedAt);
  }
}
