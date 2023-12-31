import { Component, OnInit } from '@angular/core';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { ModalController } from '@ionic/angular';
import {
  GqlContentCategoryTag,
  GqlVisibility,
  Maybe,
} from '../../../generated/graphql';
import { BucketService } from '../../services/bucket.service';
import {
  FilterData,
  Filters,
} from '../../components/filter-toolbar/filter-toolbar.component';
import { ProfileService } from 'src/app/services/profile.service';
import { AuthService } from 'src/app/services/auth.service';
import { FormControl } from '@angular/forms';
import { enumToKeyValue, toOrderBy } from '../feeds/feeds.page';
import { OpmlService } from '../../services/opml.service';
import { BasicBucket, BasicNativeFeed } from '../../graphql/types';
import { ActivatedRoute } from '@angular/router';

interface BucketFilterValues {
  tag: GqlContentCategoryTag;
  visibility: GqlVisibility;
}

@Component({
  selector: 'app-agents-page',
  templateUrl: './agents.page.html',
  styleUrls: ['./agents.page.scss'],
})
export class AgentsPage implements OnInit {
  filters: Filters<BucketFilterValues> = {
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
  };
  gridLayout = false;
  entities: { bucket?: Maybe<BasicBucket>; feed?: Maybe<BasicNativeFeed> }[] =
    [];
  isLast = false;
  private filterData: FilterData<{
    tag: GqlContentCategoryTag;
    visibility: GqlVisibility;
  }>;
  private currentPage = 0;

  constructor(
    private readonly apollo: ApolloClient<any>,
    private readonly bucketService: BucketService,
    private readonly modalCtrl: ModalController,
    private readonly profileService: ProfileService,
    private readonly opmlService: OpmlService,
    private readonly authService: AuthService,
    private readonly activatedRoute: ActivatedRoute,
  ) {}

  ngOnInit() {
    this.activatedRoute.url.subscribe(async () => {
      this.entities = [];
      this.currentPage = 0;
      await this.refetch('network-only');
    });
  }

  async firstPage(
    filterData: FilterData<{
      tag: GqlContentCategoryTag;
      visibility: GqlVisibility;
    }>,
  ) {
    this.filterData = filterData;
    await this.fetch(filterData, this.currentPage);
  }

  private async fetch(
    filterData: FilterData<BucketFilterValues>,
    page: number,
    fetchPolicy: FetchPolicy = 'cache-first',
  ): Promise<void> {
    const entities = await this.bucketService.searchBucketsOrFeeds(
      {
        cursor: {
          page,
        },
        orderBy: toOrderBy(filterData.sortBy),
      },
      fetchPolicy,
    );
    this.isLast = this.entities.length < 10;

    this.entities.push(...entities);
  }

  private async refetch(fetchPolicy: FetchPolicy = 'cache-first') {
    console.log('refetch');
    if (this.filterData) {
      await this.fetch(this.filterData, this.currentPage, fetchPolicy);
    }
  }

  createAgent() {}
}
