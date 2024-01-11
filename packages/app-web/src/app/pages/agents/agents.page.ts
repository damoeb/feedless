import { Component, OnInit } from '@angular/core';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { ModalController } from '@ionic/angular';
import { GqlContentCategoryTag, GqlVisibility, Maybe } from '../../../generated/graphql';
import { BucketService } from '../../services/bucket.service';
import { FilterData } from '../../components/filter-toolbar/filter-toolbar.component';
import { ProfileService } from 'src/app/services/profile.service';
import { AuthService } from 'src/app/services/auth.service';
import { OpmlService } from '../../services/opml.service';
import { BasicBucket, BasicNativeFeed } from '../../graphql/types';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-agents-page',
  templateUrl: './agents.page.html',
  styleUrls: ['./agents.page.scss'],
})
export class AgentsPage implements OnInit {
  gridLayout = false;
  entities: { bucket?: Maybe<BasicBucket>; feed?: Maybe<BasicNativeFeed> }[] =
    [];
  isLast = false;
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
    await this.fetch(this.currentPage);
  }

  private async fetch(
    page: number,
    fetchPolicy: FetchPolicy = 'cache-first',
  ): Promise<void> {
    const entities = await this.bucketService.searchBucketsOrFeeds(
      {
        cursor: {
          page,
        },
      },
      fetchPolicy,
    );
    this.isLast = this.entities.length < 10;

    this.entities.push(...entities);
  }

  private async refetch(fetchPolicy: FetchPolicy = 'cache-first') {
    console.log('refetch');
    await this.fetch(this.currentPage, fetchPolicy);
  }

  createAgent() {}
}
