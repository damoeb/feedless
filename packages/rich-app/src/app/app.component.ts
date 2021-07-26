import { Component } from '@angular/core';
import { BucketService } from './services/bucket.service';
import { GqlBucket, GqlNotebook } from '../generated/graphql';
import { ToastService } from './services/toast.service';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss'],
})
export class AppComponent {
  public buckets: GqlBucket[] = [];
  public notebooks: GqlNotebook[] = [];

  constructor(
    private readonly bucketService: BucketService,
    private readonly toastService: ToastService
  ) {
    this.bucketService
      .getBucketsForUser()
      .valueChanges.subscribe(({ data, error, loading }) => {
        if (loading) {
        } else if (error) {
          toastService.errorFromApollo(error);
        } else {
          console.log(data);

          this.notebooks = data.findFirstUser.notebooks;
          this.buckets = data.findFirstUser.buckets;
        }
      });
  }
}
