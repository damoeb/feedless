import { Component, OnInit } from '@angular/core';
import { GqlBucket } from '../../../generated/graphql';
import { BucketService } from '../../services/bucket.service';

@Component({
  selector: 'app-add-bucket',
  templateUrl: './add-bucket.page.html',
  styleUrls: ['./add-bucket.page.scss'],
})
export class AddBucketPage implements OnInit {
  private bucket: Partial<GqlBucket>;
  constructor(private readonly bucketService: BucketService) {}

  ngOnInit() {}

  bucketChanged(bucket: Partial<GqlBucket>) {
    this.bucket = bucket;
  }

  saveBucket() {
    this.bucketService
      .createBucket(this.bucket)
      .subscribe(console.log, console.error);
    // todo reload
  }
}
