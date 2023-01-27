import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BasicNativeFeed, FeedService } from '../../services/feed.service';
import { Pagination } from '../../services/pagination.service';
import { FilterQuery } from '../../components/filter-toolbar/filter-toolbar.component';

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
        where: {},
        page: 0
      });
      this.feeds = response.nativeFeeds;
      this.pagination = response.pagination;
      this.changeRef.detectChanges();
    });
  }

  loadMoreFeeds($event: any) {}

  getHost(url: string): string {
    return new URL(url).hostname;
  }

  search($event: FilterQuery) {
    // todo mag
  }
}
