import { AfterViewInit, Component, OnInit } from '@angular/core';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { ModalController, ToastController } from '@ionic/angular';
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
import { BucketCreateModalComponent } from '../../modals/bucket-create-modal/bucket-create-modal.component';
import { enumToKeyValue, toOrderBy } from '../feeds/feeds.page';
import { OpmlService } from '../../services/opml.service';
import { ImportModalComponent } from '../../modals/import-modal/import-modal.component';
import { visibilityToLabel } from './bucket/bucket.page';
import { BasicBucket, BasicNativeFeed } from '../../graphql/types';
import { ActivatedRoute } from '@angular/router';
import { ExportModalComponent } from '../../modals/export-modal/export-modal.component';

interface BucketFilterValues {
  tag: GqlContentCategoryTag;
  visibility: GqlVisibility;
}

@Component({
  selector: 'app-buckets-page',
  templateUrl: './buckets.page.html',
  styleUrls: ['./buckets.page.scss'],
})
export class BucketsPage implements OnInit {
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
    private readonly toastCtrl: ToastController
  ) {}

  ngOnInit() {
    this.activatedRoute.url.subscribe(async () => {
      this.entities = [];
      this.currentPage = 0;
      await this.refetch();
    });
  }

  async showCreateBucketModal() {
    const modal = await this.modalCtrl.create({
      component: BucketCreateModalComponent,
      showBackdrop: true,
    });
    await modal.present();
    const { data, role } = await modal.onDidDismiss();

    switch (role) {
      case 'save':
        await this.bucketService.createBuckets({
          buckets: [
            {
              title: data.title,
              websiteUrl: data.websiteUrl,
              imageUrl: data.imageUrl,
              tags: data.tags,
              description: data.description,
              visibility: GqlVisibility.IsPublic,
            },
          ],
        });
        const toast = await this.toastCtrl.create({
          message: 'Bucket created',
          duration: 3000,
          color: 'success',
        });

        await toast.present();
        await this.refetch();
        break;
    }
  }

  async handleImport(): Promise<void> {
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

  label(visibility: GqlVisibility): string {
    return visibilityToLabel(visibility);
  }

  isOwner(entity: BasicBucket): boolean {
    return this.profileService.getUserId() === entity.ownerId;
  }

  async nextPage(event: any) {
    this.currentPage += 1;
    await this.refetch();
    await event.target.complete();
  }

  async firstPage(
    filterData: FilterData<{
      tag: GqlContentCategoryTag;
      visibility: GqlVisibility;
    }>
  ) {
    this.filterData = filterData;
    await this.fetch(filterData, this.currentPage);
  }

  private async fetch(
    filterData: FilterData<BucketFilterValues>,
    page: number,
    fetchPolicy: FetchPolicy = 'cache-first'
  ): Promise<void> {
    const entities = await this.bucketService.searchBucketsOrFeeds(
      {
        cursor: {
          page,
        },
        orderBy: toOrderBy(filterData.sortBy),
      },
      fetchPolicy
    );
    this.isLast = this.entities.length < 20;

    this.entities.push(...entities);
  }

  private async refetch(fetchPolicy: FetchPolicy = 'cache-first') {
    if (this.filterData) {
      await this.fetch(this.filterData, this.currentPage, fetchPolicy);
    }
  }
}
