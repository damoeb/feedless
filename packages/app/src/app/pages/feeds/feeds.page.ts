import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BasicNativeFeed, FeedService } from '../../services/feed.service';
import {
  FilterData,
  FilterOption,
  Filters,
} from '../../components/filter-toolbar/filter-toolbar.component';
import { FilteredList } from '../../components/filtered-list';
import { ActionSheetButton, ActionSheetController } from '@ionic/angular';
import { Pagination } from 'src/app/services/pagination.service';
import { FormControl } from '@angular/forms';
import {
  GqlContentCategoryTag,
  GqlContentSortTag,
  GqlNativeFeedStatus,
  GqlOrderByInput,
  GqlSortOrder,
  GqlVisibility,
} from '../../../generated/graphql';

export interface FeedFilterValues {
  tag: GqlContentCategoryTag;
  visibility: GqlVisibility;
  status: GqlNativeFeedStatus;
}

export const toOrderBy = (sortBy: GqlContentSortTag): GqlOrderByInput => {
  switch (sortBy) {
    case GqlContentSortTag.Newest:
      return {
        createdAt: GqlSortOrder.Desc,
      };
    case GqlContentSortTag.Oldest:
      return {
        createdAt: GqlSortOrder.Asc,
      };
  }
};

export const enumToKeyValue = <T>(theEnum: T): FilterOption[] =>
  Object.keys(theEnum).map((name) => ({
    key: theEnum[name],
    value: name,
  }));

@Component({
  selector: 'app-feeds-page',
  templateUrl: './feeds.page.html',
  styleUrls: ['./feeds.page.scss'],
})
export class FeedsPage extends FilteredList<
  BasicNativeFeed,
  FilterData<FeedFilterValues>
> {
  filters: Filters<FeedFilterValues> = {
    tag: {
      name: 'tag',
      control: new FormControl<GqlContentCategoryTag[]>([]),
      options: enumToKeyValue(GqlContentCategoryTag),
    },
    visibility: {
      name: 'visibility',
      control: new FormControl<GqlVisibility[]>([]),
      options: enumToKeyValue(GqlVisibility),
    },
    status: {
      name: 'status',
      control: new FormControl<GqlNativeFeedStatus[]>([]),
      options: enumToKeyValue(GqlNativeFeedStatus),
    },
  };

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly feedService: FeedService,
    readonly actionSheetCtrl: ActionSheetController
  ) {
    super(actionSheetCtrl);
  }

  getBulkActionButtons(): ActionSheetButton<any>[] {
    return [];
  }
  async fetch(
    filterData: FilterData<FeedFilterValues>,
    page: number
  ): Promise<[BasicNativeFeed[], Pagination]> {
    const response = await this.feedService.searchNativeFeeds({
      where: {
        query: '',
        status: {
          oneOf: filterData.filters.status,
        },
        visibility: {
          oneOf: filterData.filters.visibility,
        },
      },
      orderBy: toOrderBy(filterData.sortBy),
      page,
    });
    return [response.nativeFeeds, response.pagination];
  }

  getHost(url: string): string {
    return new URL(url).hostname;
  }
}
