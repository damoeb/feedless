import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { GqlBucket } from '../../../generated/graphql';

@Component({
  selector: 'app-bucket',
  templateUrl: './bucket.component.html',
  styleUrls: ['./bucket.component.scss'],
})
export class BucketComponent implements OnInit {
  public bucket: Partial<GqlBucket> = {
    title: '',
    description: '',
    listed: true,
  };

  @Output()
  public bucketChanged = new EventEmitter<Partial<GqlBucket>>();

  constructor() {}

  ngOnInit() {}

  emitUpdate() {
    this.bucketChanged.emit(this.bucket);
  }
}
