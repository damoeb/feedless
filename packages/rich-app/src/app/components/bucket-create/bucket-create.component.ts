import { Component, OnInit } from '@angular/core';
import { GqlBucket } from '../../../generated/graphql';
import { BucketService } from '../../services/bucket.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-bucket-create',
  templateUrl: './bucket-create.component.html',
  styleUrls: ['./bucket-create.component.scss'],
})
export class BucketCreateComponent implements OnInit {
  constructor(
    private readonly bucketService: BucketService,
    private readonly toastService: ToastService
  ) {}

  ngOnInit() {}

  // createBucket() {
  //   this.bucketService
  //     .createBucket(this.bucket)
  //     .toPromise()
  //     .then(({ data, errors }) => {
  //       if (errors) {
  //         this.toastService.errors(errors);
  //       } else {
  //         // todo open bucket
  //         this.toastService.info('Subscribed');
  //       }
  //     });
  // }
}
