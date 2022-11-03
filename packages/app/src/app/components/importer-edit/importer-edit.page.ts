import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ActualBucket, ActualImporter, BucketService } from '../../services/bucket.service';
import { ActivatedRoute } from '@angular/router';
import { BubbleColor } from '../bubble/bubble.component';
import { ArticleService } from '../../services/article.service';
import { ActualNativeFeed } from '../../services/feed.service';

@Component({
  selector: 'app-bucket-edit',
  templateUrl: './importer-edit.page.html',
  styleUrls: ['./importer-edit.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ImporterEditPage implements OnInit {

  formGroup: FormGroup<{ website: FormControl<string | null>; description: FormControl<string | null>; name: FormControl<string | null> }>;
  private loadingBucket: boolean;
  bucket: ActualBucket;
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
    this.loadingBucket = true;
    try {
      this.bucket = await this.bucketService.getBucketById(bucketId);
    } finally {
      this.loadingBucket = false;
    }
    this.changeRef.detectChanges();
  }

  saveBucket() {

  }

  getColorForImporter(importer: ActualImporter): BubbleColor {
    if (importer.active) {
      if (importer.feed.status === 'OK') {
        return 'green'
      } else {
        return 'red'
      }
    } else {
      return 'gray'
    }
  }

  lastUpdatedAt(feed: ActualNativeFeed): Date {
    return new Date(feed.lastUpdatedAt);
  }
}
