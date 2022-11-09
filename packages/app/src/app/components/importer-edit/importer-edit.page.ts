import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Bucket, BucketService } from '../../services/bucket.service';
import { ActivatedRoute } from '@angular/router';
import { BubbleColor } from '../bubble/bubble.component';
import { NativeFeed } from '../../services/feed.service';
import { Importer } from '../../services/importer.service';

@Component({
  selector: 'app-bucket-edit',
  templateUrl: './importer-edit.page.html',
  styleUrls: ['./importer-edit.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ImporterEditPage implements OnInit {

  formGroup: FormGroup<{ website: FormControl<string | null>; description: FormControl<string | null>; name: FormControl<string | null> }>;
  private loading: boolean;
  bucket: Bucket;
  feedIdInFocus: string;

  constructor(private readonly bucketService: BucketService,
              private readonly activatedRoute: ActivatedRoute,
              private readonly changeRef: ChangeDetectorRef) {
    this.formGroup = new FormGroup({
      name: new FormControl('', Validators.required),
      description: new FormControl('', Validators.required),
      website: new FormControl('', Validators.required),
    });
  }

  ngOnInit() {
    this.activatedRoute.params.subscribe(params => {
      this.initBucket(params.id);
      this.feedIdInFocus = params.feedId;
    })
  }

  private async initBucket(bucketId: string) {
    this.loading = true;
    try {
      this.bucket = await this.bucketService.getBucketById(bucketId);
    } finally {
      this.loading = false;
    }
    this.changeRef.detectChanges();
  }

  saveBucket() {

  }

  getColorForImporter(importer: Importer): BubbleColor {
    if (importer.autoRelease) {
      if (importer.feed.status === 'OK') {
        return 'green'
      } else {
        return 'red'
      }
    } else {
      return 'gray'
    }
  }

  lastUpdatedAt(feed: NativeFeed): Date {
    return new Date(feed.lastUpdatedAt);
  }
}
