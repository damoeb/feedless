import { Component, OnInit } from '@angular/core';
import { ModalController, PopoverController } from '@ionic/angular';
import { ChooseFeedUrlComponent } from '../choose-feed-url/choose-feed-url.component';
import {
  GqlBucket,
  GqlNativeFeedRef,
  GqlProxyFeed,
} from '../../../generated/graphql';
import { ChooseBucketComponent } from '../choose-bucket/choose-bucket.component';
import { SubscriptionService } from '../../services/subscription.service';
import { ToastService } from '../../services/toast.service';
import { ProfileMenuComponent } from '../profile-menu/profile-menu.component';

@Component({
  selector: 'app-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.scss'],
})
export class ToolbarComponent implements OnInit {
  query: string;

  constructor(
    private readonly modalController: ModalController,
    private readonly toastService: ToastService,
    private readonly popoverController: PopoverController,
    private readonly subscriptionService: SubscriptionService
  ) {}

  ngOnInit() {}

  async addUrl() {
    const modal = await this.modalController.create({
      component: ChooseFeedUrlComponent,
    });
    await modal.present();
    const responseFeed = await modal.onDidDismiss<
      GqlNativeFeedRef | GqlProxyFeed
    >();
    // console.log('chose feed', responseFeed.data);
    if (responseFeed.data) {
      const modal = await this.modalController.create({
        component: ChooseBucketComponent,
        backdropDismiss: false,
      });
      await modal.present();
      const responseBucket = await modal.onDidDismiss<GqlBucket>();
      if (responseBucket.data) {
        await this.subscriptionService
          .createSubscription(responseFeed.data, responseBucket.data.id)
          .toPromise();
        this.toastService.info('Subscribed');
      }
    }
  }

  async showProfileMenu(event: any) {
    const popover = await this.popoverController.create({
      component: ProfileMenuComponent,
      event,
      translucent: true,
    });
    await popover.present();

    const { role } = await popover.onDidDismiss();
  }
}
