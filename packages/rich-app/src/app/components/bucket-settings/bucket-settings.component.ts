import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import { split, compact } from 'lodash';
import { ModalController } from '@ionic/angular';
import { GqlBucket, GqlSubscription } from '../../../generated/graphql';
import { AddSubscriptionComponent } from '../add-subscription/add-subscription.component';
import { BucketService } from '../../services/bucket.service';

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
    subscription: 2,
    throttle: 3,
  };
  currentAccordion: number = this.accordion.subscription;

  constructor(
    private readonly modalController: ModalController,
    private readonly bucketService: BucketService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {}

  ngOnInit() {}

  async dismissModal() {
    await this.modalController.dismiss();
  }

  addSubscription() {
    return this.openSubscriptionModal();
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
      component: AddSubscriptionComponent,
      componentProps: {
        subscription,
        bucket: this.bucket,
      },
    });

    await modal.present();
    modal.onDidDismiss().then(({ data }) => {
      switch (data) {
        case 'subscribe':
        case 'unsubscribe':
        case 'update':
          this.refreshBucketData();
          break;
        default:
          console.log(`Ignoring dismiss-reason ${data}`);
          break;
      }
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

  someUnhealthy(bucket: GqlBucket) {
    return (
      bucket.subscriptions.some(
        (subscription) => subscription?.feed?.status !== 'ok'
      ) || bucket.subscriptions.length === 0
    );
  }

  addPostProcessor() {}
}
