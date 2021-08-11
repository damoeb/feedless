import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import { ModalController } from '@ionic/angular';
import { GqlBucket, GqlSubscription } from '../../../generated/graphql';
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
  bucket: GqlBucket;
  private unchangedBucket: Partial<GqlBucket>;
  private changed = false;
  managementUrl: string;
  private readonly relevantFields = ['title', 'description', 'listed'];

  constructor(
    private readonly modalController: ModalController,
    private readonly bucketService: BucketService,
    private readonly subscriptionService: SubscriptionService,
    private readonly toastService: ToastService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.managementUrl = `http://localhost:8080/stream:${this.bucket.id}`;
    this.unchangedBucket = clone(pick(this.bucket, this.relevantFields));
  }

  async dismissModal() {
    await this.modalController.dismiss(this.hasChanged());
  }

  private refreshBucketData() {
    this.bucketService
      .getBucketsById(this.bucket.id)
      .subscribe(({ data, error }) => {
        console.log('update bucket', data);
        this.bucket = data.bucket;
        this.unchangedBucket = clone(pick(this.bucket, this.relevantFields));
        this.changeDetectorRef.detectChanges();
      });
  }

  isHealthy(subscription: GqlSubscription): boolean {
    return subscription?.feed?.status === 'ok';
  }

  addPostProcessor() {}

  save() {
    return this.bucketService
      .updateBucket(this.bucket)
      .toPromise()
      .then(async () => {
        await this.toastService.info('Saved');
        return this.dismissModal();
      })
      .catch((e) => this.toastService.errorFromApollo(e));
  }

  hasBrokenSubscriptions(): boolean {
    return this.bucket.subscriptions.some((s) => s.feed.broken);
  }

  deleteBucket() {
    this.bucketService.delteById(this.bucket.id);
    return this.modalController.dismiss(true);
  }

  hasChanged(): boolean {
    return this.changed || this.hasDirectChanges();
  }

  hasDirectChanges(): boolean {
    let filterFields = (b) => pick(b, this.relevantFields);
    return !isEqual(filterFields(this.bucket), this.unchangedBucket);
  }

  showWebhooks() {}

  async showFilters() {
    const modal = await this.modalController.create({
      component: FiltersComponent,
      backdropDismiss: false,
      componentProps: {
        bucket: this.bucket,
      },
    });

    await modal.present();
    await modal.onDidDismiss();
    this.refreshBucketData();
  }

  showRestControl() {}

  async showThrottle() {
    const modal = await this.modalController.create({
      component: OutputThrottleComponent,
      backdropDismiss: false,
      componentProps: {
        bucket: this.bucket,
      },
    });

    await modal.present();
    await modal.onDidDismiss();
    this.refreshBucketData();
  }

  async showSubscriptions() {
    const modal = await this.modalController.create({
      component: SubscriptionsComponent,
      backdropDismiss: false,
      componentProps: {
        bucket: this.bucket,
      },
    });

    await modal.present();
    await modal.onDidDismiss();
    this.refreshBucketData();
  }

  hasDescription(): boolean {
    return (this.bucket.description || '').trim().length > 0;
  }

  showHiddenMenu() {}
}
