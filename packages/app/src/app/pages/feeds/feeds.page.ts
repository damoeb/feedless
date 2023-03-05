import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BasicNativeFeed, FeedService } from '../../services/feed.service';
import {
  FilterQuery,
  Filters,
} from '../../components/filter-toolbar/filter-toolbar.component';
import { FilteredList } from '../../components/filtered-list';
import { ActionSheetButton, ActionSheetController } from '@ionic/angular';
import { Pagination } from 'src/app/services/pagination.service';
import { FormControl } from '@angular/forms';
import {
  GqlArticleReleaseStatus,
  GqlArticleType,
  GqlContentCategoryTag,
  GqlContentTypeTag,
} from '../../../generated/graphql';

@Component({
  selector: 'app-feeds-page',
  templateUrl: './feeds.page.html',
  styleUrls: ['./feeds.page.scss'],
})
export class FeedsPage extends FilteredList<BasicNativeFeed, FilterQuery> {
  filters: Filters = {
    tag: {
      name: 'tag',
      control: new FormControl<GqlContentCategoryTag[]>([]),
      options: Object.values(GqlContentCategoryTag),
    },
    content: {
      name: 'content',
      control: new FormControl<GqlContentTypeTag[]>(
        Object.values(GqlContentTypeTag)
      ),
      options: Object.values(GqlContentTypeTag),
    },
    status: {
      name: 'status',
      control: new FormControl<GqlArticleReleaseStatus[]>([
        GqlArticleReleaseStatus.Released,
      ]),
      options: Object.values(GqlArticleReleaseStatus),
    },
    type: {
      name: 'type',
      control: new FormControl<GqlArticleType[]>([GqlArticleType.Feed]),
      options: Object.values(GqlArticleType),
    },
  };

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly feedService: FeedService,
    readonly actionSheetCtrl: ActionSheetController
  ) {
    super('feed', actionSheetCtrl);
  }

  getBulkActionButtons(): ActionSheetButton<any>[] {
    return [];
  }
  async fetch(
    filterData: FilterQuery
  ): Promise<[BasicNativeFeed[], Pagination]> {
    const response = await this.feedService.searchNativeFeeds({
      where: {
        query: filterData.query,
      },
      page: 0,
    });
    return [response.nativeFeeds, response.pagination];
  }

  getHost(url: string): string {
    return new URL(url).hostname;
  }
}
