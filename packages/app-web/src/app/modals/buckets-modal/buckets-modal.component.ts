import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import { IonInfiniteScroll, ModalController } from '@ionic/angular';
import {
  BasicBucket,
  Bucket,
  BucketData,
  Pagination,
} from '../../graphql/types';
import { BucketService } from '../../services/bucket.service';
import { FormControl } from '@angular/forms';
import { debounce, interval } from 'rxjs';
import { ModalService } from '../../services/modal.service';

export interface BucketsModalComponentProps {
  bucket?: Bucket;
  onClickBucket: (bucket: BasicBucket) => Promise<void>;
}

@Component({
  selector: 'app-buckets-modal',
  templateUrl: './buckets-modal.component.html',
  styleUrls: ['./buckets-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BucketsModalComponent
  implements BucketsModalComponentProps, OnInit
{
  bucket?: Bucket;

  @Input()
  onClickBucket: (bucket: BasicBucket) => Promise<void>;

  private data: BucketData;
  protected buckets: Array<BasicBucket> = [];
  protected pagination: Pagination;
  protected queryFC: FormControl<string>;
  private query: string;

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly changeRef: ChangeDetectorRef,
    private readonly modalService: ModalService,
    private readonly bucketService: BucketService,
  ) {}

  async ngOnInit() {
    this.queryFC = new FormControl<string>('');
    this.queryFC.valueChanges
      .pipe(debounce(() => interval(800)))
      .subscribe(async (query) => {
        this.query = query;
        await this.fetchBuckets(0);
      });
    await this.fetchBuckets(0);
  }

  async fetchBuckets(page: number) {
    const { buckets, pagination } = await this.bucketService.search({
      cursor: {
        page,
      },
      // where: {
      //
      // }
    });
    this.buckets.push(...buckets);
    this.pagination = pagination;
    this.changeRef.detectChanges();
  }

  dismissModal() {
    return this.modalCtrl.dismiss();
  }

  async loadMore(event: IonInfiniteScroll) {
    if (!this.pagination.isLast) {
      await this.fetchBuckets(this.pagination.page + 1);
    }
    await event.complete();
  }

  async createBucket() {
    const bucket = await this.modalService.openCreateBucketModal();
    console.log('createBucket', bucket);
    if (bucket) {
      await this.onClickBucket(bucket);
    }
  }
}
