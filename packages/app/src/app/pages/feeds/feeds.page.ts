import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {
  BasicNativeFeed,
  FeedService,
  PagedNativeFeeds,
} from '../../services/feed.service';
import { Pagination } from '../../services/pagination.service';

@Component({
  selector: 'app-bucket',
  templateUrl: './feeds.page.html',
  styleUrls: ['./feeds.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedsPage implements OnInit {
  id: string;
  name: string;
  feeds: Array<BasicNativeFeed>;
  pagination: Pagination;
  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly changeRef: ChangeDetectorRef,
    private readonly feedService: FeedService
  ) {}

  ngOnInit() {
    this.activatedRoute.params.subscribe(async (params) => {
      this.id = params.id;
      const response = await this.feedService.searchNativeFeeds({
        query: '',
      });
      this.feeds = response.nativeFeeds;
      this.pagination = response.pagination;
      this.changeRef.detectChanges();
    });
  }

  loadMoreFeeds($event: any) {}
}
