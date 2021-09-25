import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { GqlBucket } from '../../../generated/graphql';
import { ActivatedRoute } from '@angular/router';
import { BucketService } from '../../services/bucket.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-buckets',
  templateUrl: './buckets.page.html',
  styleUrls: ['./buckets.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BucketsPage implements OnInit {

  public buckets: GqlBucket[];
  loading = false;

  constructor(
    private activatedRoute: ActivatedRoute,
    private bucketService: BucketService,
    private changeDetectorRef: ChangeDetectorRef,
    private toastService: ToastService,
  ) {}

  ngOnInit() {
    this.reload();
  }

  reload() {
    this.loading = true;
    this.bucketService
      .getBucketsForUser()
      .valueChanges
      .subscribe(({ data, error }) => {
        if (error) {
          this.toastService.errorFromApollo(error);
        } else {
          this.buckets = data.findFirstUser.buckets;
        }
        this.loading = false;
        this.changeDetectorRef.detectChanges();
      });
  }
}
