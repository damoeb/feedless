import {Component} from '@angular/core';
import {BucketService} from './services/bucket.service';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss'],
})
export class AppComponent {
  public appPages = [
    // {title: 'Inbox', url: '/folder/Inbox', icon: 'mail'},
    // {title: 'Outbox', url: '/folder/Outbox', icon: 'paper-plane'},
    {title: 'Favorites', url: '/folder/Favorites', icon: 'heart', count: 4},
    {title: 'Archived', url: '/folder/Archived', icon: 'archive', count: 0},
    {title: 'Drafts', url: '/folder/Drafts', count: 0}
  ];
  public buckets = [];
  public labels = ['Family', 'Friends', 'Notes', 'Work', 'Travel', 'Reminders'];

  constructor(private readonly bucketService: BucketService) {
    this.bucketService.getBucketsForUser().valueChanges.subscribe(({ data, loading }) => {
      // console.log(data.findFirstUser.buckets);
      this.buckets = data.findFirstUser.buckets.map(bucket => {
        return {
          title: bucket.title,
          url: `/bucket/${bucket.id}`
        };
      });
    });
  }
}
