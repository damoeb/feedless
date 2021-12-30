import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { firstValueFrom } from 'rxjs';
import { GqlBucket, GqlFeed, GqlSubscription } from '../../../generated/graphql';
import { clone, isEqual, pick } from 'lodash';
import { BucketService } from '../../services/bucket.service';
import { SubscriptionService } from '../../services/subscription.service';
import { ToastService } from '../../services/toast.service';
import { FiltersComponent } from '../filters/filters.component';
import { OutputThrottleComponent } from '../output-throttle/output-throttle.component';
import { SubscriptionsComponent } from '../subscriptions/subscriptions.component';

@Component({
  selector: 'app-bucket-settings',
  templateUrl: './bucket-settings.component.html',
  styleUrls: ['./bucket-settings.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BucketSettingsComponent implements OnInit {
  @Input()
  bucket: GqlBucket & { subscriptions: Array<(
      GqlSubscription
      & { feed: GqlFeed }
      )> };
  private unchangedBucket: Partial<GqlBucket>;
  private changed = false;
  private readonly relevantFields = [
    'title',
    'description',
    'listed',
    'in_focus',
  ];

  constructor(
    private readonly modalController: ModalController,
    private readonly bucketService: BucketService,
    private readonly subscriptionService: SubscriptionService,
    private readonly toastService: ToastService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.unchangedBucket = clone(pick(this.bucket, this.relevantFields));
    this.reloadBucketData();
  }

  async dismissModal() {
    await this.modalController.dismiss(this.hasChanged());
  }

  private reloadBucketData() {
    this.bucketService
      .getBucketsById(this.bucket.id)
      .subscribe(({ data, error }) => {
        console.log('refresh bucket data', data);
        this.bucket = data.bucket as GqlBucket & { subscriptions: Array<(
            GqlSubscription
            & { feed: GqlFeed }
            )> };
        this.unchangedBucket = clone(pick(this.bucket, this.relevantFields));
        this.changeDetectorRef.detectChanges();
      });
  }

  isHealthy(subscription: GqlSubscription): boolean {
    return subscription?.feed?.status === 'ok';
  }

  addPostProcessor() {}

  save() {
    return firstValueFrom(this.bucketService
      .updateBucket(this.bucket)
      )
      .then(async () => {
        await this.toastService.info('Saved');
        return this.dismissModal();
      })
      .catch((e) => this.toastService.errorFromApollo(e));
  }

  hasBrokenSubscriptions(): boolean {
    return this.bucket.subscriptions.some((s) => s.feed.broken);
  }

  async deleteBucket() {
    await this.bucketService.deleteById(this.bucket.id).toPromise();
    await this.toastService.info('Deleted');
    return this.modalController.dismiss(true);
  }

  hasChanged(): boolean {
    return this.changed || this.hasDirectChanges();
  }

  hasDirectChanges(): boolean {
    const filterFields = (b) => pick(b, this.relevantFields);
    return !isEqual(filterFields(this.bucket), this.unchangedBucket);
  }

  showWebhooks() {}

  async showFilters() {
    console.log('open FiltersComponent');
    const modal = await this.modalController.create({
      component: FiltersComponent,
      backdropDismiss: false,
      componentProps: {
        bucket: this.bucket,
      },
    });

    await modal.present();
    await modal.onDidDismiss();
    this.reloadBucketData();
  }

  async showThrottle() {
    console.log('open OutputThrottleComponent');
    const modal = await this.modalController.create({
      component: OutputThrottleComponent,
      backdropDismiss: false,
      componentProps: {
        bucket: this.bucket,
      },
    });

    await modal.present();
    await modal.onDidDismiss();
    this.reloadBucketData();
  }

  async showSubscriptions() {
    console.log('open SubscriptionsComponent');
    const modal = await this.modalController.create({
      component: SubscriptionsComponent,
      componentProps: {
        bucket: this.bucket,
      },
    });

    await modal.present();
    await modal.onDidDismiss();
    this.reloadBucketData();
  }

  hasDescription(): boolean {
    return (this.bucket.description || '').trim().length > 0;
  }

  showHiddenMenu() {}
}
