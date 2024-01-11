import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BucketService } from '../../../services/bucket.service';
import {
  AlertController,
  ModalController,
  ToastController,
} from '@ionic/angular';
import { FetchPolicy } from '@apollo/client/core';
import {
  BucketCreateModalComponent,
  BucketCreateModalComponentProps,
} from '../../../modals/bucket-create-modal/bucket-create-modal.component';
import { GqlVisibility } from '../../../../generated/graphql';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { FilterData } from '../../../components/filter-toolbar/filter-toolbar.component';
import { ProfileService } from '../../../services/profile.service';
import { Subscription } from 'rxjs';
import { Bucket, SourceSubscription } from '../../../graphql/types';
import { SourceSubscriptionService } from '../../../services/source-subscription.service';
import { ModalService } from '../../../services/modal.service';

export const visibilityToLabel = (visibility: GqlVisibility): string => {
  switch (visibility) {
    case GqlVisibility.IsPrivate:
      return 'private';
    case GqlVisibility.IsPublic:
      return 'public';
  }
};

@Component({
  selector: 'app-bucket-page',
  templateUrl: './bucket.page.html',
  styleUrls: ['./bucket.page.scss'],
})
export class BucketPage implements OnInit, OnDestroy {
  loadingBucket: boolean;
  bucket: SourceSubscription;
  query = '';
  readonly entityVisibility = GqlVisibility;
  isOwner: boolean;
  feedUrl: string;

  private subscriptions: Subscription[] = [];

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly router: Router,
    private readonly toastCtrl: ToastController,
    private readonly alertCtrl: AlertController,
    private readonly sourceSubscriptionService: SourceSubscriptionService,
    private readonly profileService: ProfileService,
    private readonly serverSettings: ServerSettingsService,
    private readonly modalService: ModalService,
  ) {}

  ngOnInit() {
    this.subscriptions.push(
      this.activatedRoute.params.subscribe((params) => {
        this.fetchSourceSubscription(params.id);
        this.feedUrl = `${this.serverSettings.apiUrl}/subscription/${params.id}`;
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async editBucket() {

    await this.modalService.openFeedBuilder({
      feedBuilder: {}
    }, async (data, role) => {
      if (data) {
        const toast = await this.toastCtrl.create({
          message: 'Updated',
          duration: 3000,
          color: 'success',
        });
        await toast.present();
        await this.fetchSourceSubscription(this.bucket.id, 'network-only');
      } else {
        const toast = await this.toastCtrl.create({
          message: 'Canceled',
          duration: 3000,
        });

        await toast.present();
      }
    })
  }

  async deleteBucket() {
    let keepFeeds = false;
    const alert = await this.alertCtrl.create({
      header: '',
      backdropDismiss: false,
      message: `Delete Bucket?`,
      buttons: [
        {
          text: 'Just Bucket',
          role: 'confirm',
          handler: () => (keepFeeds = true),
        },
        {
          text: 'Delete Bucket and Feeds',
          role: 'confirm',
          handler: () => (keepFeeds = false),
        },
        {
          text: 'Cancel',
          role: 'cancel',
        },
      ],
    });

    await alert.present();
    const { role } = await alert.onDidDismiss();

    if (role !== 'cancel') {
      // await this.sourceSubscriptionService.deleteBucket(this.bucket.id, keepFeeds);
      // const toast = await this.toastCtrl.create({
      //   message: 'Deleted',
      //   duration: 3000,
      //   color: 'success',
      // });
      // await toast.present();
      // await this.router.navigateByUrl('/buckets');
    }
  }

  private async fetchSourceSubscription(
    sourceSubscriptionId: string,
    fetchPolicy: FetchPolicy = 'cache-first',
  ) {
    this.loadingBucket = true;
    try {
      this.bucket = await this.sourceSubscriptionService.getSubscriptionById(
        sourceSubscriptionId,
        fetchPolicy,
      );
      this.isOwner = this.profileService.getUserId() === this.bucket?.ownerId;
    } finally {
      this.loadingBucket = false;
    }
  }
}
