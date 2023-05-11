import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FeedService } from '../../services/feed.service';
import {
  FilterData,
  FilterOption,
  Filters,
} from '../../components/filter-toolbar/filter-toolbar.component';
import { FilteredList } from '../../components/filtered-list';
import {
  ActionSheetButton,
  ActionSheetController,
  ModalController,
} from '@ionic/angular';
import { FormControl } from '@angular/forms';
import {
  GqlContentCategoryTag,
  GqlContentSortTag,
  GqlNativeFeedStatus,
  GqlOrderByInput,
  GqlSortOrder,
  GqlVisibility,
} from '../../../generated/graphql';
import { BasicNativeFeed, Pagination } from '../../graphql/types';
import { ImportModalComponent } from '../../modals/import-modal/import-modal.component';
import { ExportModalComponent } from '../../modals/export-modal/export-modal.component';
import { FetchPolicy } from '@apollo/client/core';

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
    private readonly modalCtrl: ModalController,
    readonly actionSheetCtrl: ActionSheetController
  ) {
    super(actionSheetCtrl);
  }

  getBulkActionButtons(): ActionSheetButton<any>[] {
    return [];
  }
  async fetch(
    filterData: FilterData<FeedFilterValues>,
    page: number,
    fetchPolicy: FetchPolicy
  ): Promise<[BasicNativeFeed[], Pagination]> {
    const response = await this.feedService.searchNativeFeeds(
      {
        where: {
          status: {
            oneOf: filterData.filters.status,
          },
          visibility: {
            oneOf: filterData.filters.visibility,
          },
        },
        orderBy: toOrderBy(filterData.sortBy),
        cursor: {
          page,
        },
      },
      fetchPolicy
    );
    return [response.nativeFeeds, response.pagination];
  }

  async handleImport() {
    const modal = await this.modalCtrl.create({
      component: ImportModalComponent,
      showBackdrop: true,
    });
    await modal.present();
  }

  async handleExport() {
    const modal = await this.modalCtrl.create({
      component: ExportModalComponent,
      showBackdrop: true,
    });
    await modal.present();
  }
}
