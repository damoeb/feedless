import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ActualBucket, ActualImporter, BucketService } from '../../services/bucket.service';
import { ActivatedRoute } from '@angular/router';
import { BubbleColor } from '../bubble/bubble.component';
import { ArticleFromFeed, ArticleService } from '../../services/article.service';
import { ActualPagination } from '../../services/pagination.service';

@Component({
  selector: 'app-bucket-edit',
  templateUrl: './bucket-edit.page.html',
  styleUrls: ['./bucket-edit.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BucketEditPage implements OnInit {

  formGroup: FormGroup<{ website: FormControl<string | null>; description: FormControl<string | null>; name: FormControl<string | null> }>;
  private loadingBucket: boolean;
  bucket: ActualBucket;

  constructor(private readonly bucketService: BucketService,
              private readonly activatedRoute: ActivatedRoute,
              private readonly changeRef: ChangeDetectorRef,
              private readonly articleService: ArticleService) {
    this.formGroup = new FormGroup({
      name: new FormControl('', Validators.required),
      description: new FormControl('', Validators.required),
      website: new FormControl('', Validators.required),
    });
  }

  ngOnInit() {
    this.activatedRoute.params.subscribe(params => {
      this.initBucket(params.id);
    })
  }

  private async initBucket(bucketId: string) {
    this.loadingBucket = true;
    try {
      this.bucket = await this.bucketService.getBucketById(bucketId);
    } finally {
      this.loadingBucket = false;
    }
    console.log('edit bucket', this.bucket)
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

  getActivityColor(importer: ActualImporter): string {
    if (importer.active) {
      return 'success'
    }
    return 'danger'
  }
}
