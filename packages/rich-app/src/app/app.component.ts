import { Component } from '@angular/core';
import { BucketService } from './services/bucket.service';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss'],
})
export class AppComponent {
  public appPages = [
    { title: 'Favorites', url: '/folder/favorites', icon: 'heart', count: 4 },
    { title: 'Notes', url: '/folder/notes', icon: 'archive', count: 0 },
    { title: 'Drafts', url: '/folder/drafts', count: 0 },
  ];
  public buckets = [];

  constructor(private readonly bucketService: BucketService) {
    this.bucketService
      .getBucketsForUser()
      .valueChanges.subscribe(({ data, error, loading }) => {
        if (loading) {
        } else if (error) {
          console.error(error);
        } else {
          console.log(data);
          this.buckets = data.findFirstUser.buckets.map((bucket) => {
            return {
              title: bucket.title,
              url: `/bucket/${bucket.id}`,
            };
          });
        }
      });
  }
}
